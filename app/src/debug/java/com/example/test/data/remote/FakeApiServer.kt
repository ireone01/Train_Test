package com.example.test.data.remote

import android.content.Context
import android.util.Log
import com.example.test.domain.model.RequestStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import java.net.InetAddress
import java.util.Locale
import java.security.SecureRandom

/** Debug-only mock backend. MockWebServer handles the HTTP protocol; this class handles routes. */
class FakeApiServer(context: Context) : LocalApiServer {
    private val server = MockWebServer()
    private val gson = Gson()
    private val random = SecureRandom()
    private val preferences = context.applicationContext.getSharedPreferences("fake_inventory_api", Context.MODE_PRIVATE)
    private val equipmentType = object : TypeToken<List<EquipmentDto>>() {}.type
    private val requestType = object : TypeToken<List<BorrowRequestDto>>() {}.type
    private val equipment = loadList("equipment", equipmentType, initialEquipment).toMutableList()
    private val requests = loadList<BorrowRequestDto>("requests", requestType, emptyList()).toMutableList()
    private var nextRequestId = (requests.maxOfOrNull { it.id } ?: 0) + 1

    override val baseUrl: String
        get() = server.url("/api/").toString()

    init {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val result = synchronized(this@FakeApiServer) {
                    route(
                        method = request.method.orEmpty(),
                        path = request.url.encodedPath,
                        actorRole = request.headers["X-Actor-Role"],
                        body = request.body?.utf8().orEmpty()
                    )
                }
                Log.i(TAG, "${request.method} ${request.url.encodedPath} -> ${result.first}")
                return MockResponse.Builder()
                    .code(result.first)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .body(gson.toJson(result.second))
                    .build()
            }
        }
    }

    override fun close() = server.close()

    fun start() {
        server.start(InetAddress.getByName("127.0.0.1"), 0)
        Log.i(TAG, "Mock API listening at $baseUrl")
    }

    private fun route(method: String, path: String, actorRole: String?, body: String): Pair<Int, Any> =
        try {
            routeRequest(method, path, actorRole, body)
        } catch (error: Exception) {
            val status = (error as? ApiProblem)?.statusCode ?: 500
            val message = error.message ?: "Internal server error"
            Log.w(TAG, "$method $path -> $status: $message")
            status to ApiErrorDto(message)
        }

    private fun routeRequest(method: String, path: String, actorRole: String?, body: String): Pair<Int, Any> {
        if (method == "POST" && path == "/api/auth/login") {
            val input = gson.fromJson(body, LoginRequestDto::class.java)
                ?: problem(400, "Thiếu thông tin đăng nhập")
            val role = demoAccounts[input.phone]
            if (role == null || input.password != "123") problem(401, "Số điện thoại hoặc mật khẩu không đúng")
            return 200 to LoginResponseDto(input.phone, role, "demo-token-${role.lowercase(Locale.ROOT)}")
        }
        if (method == "GET" && path == "/api/equipment") return 200 to equipment.toList()
        if (method == "GET" && path == "/api/requests") return 200 to requests.toList()

        if (method == "POST" && path == "/api/requests") {
            requireRole(actorRole, "STAFF")
            val input = gson.fromJson(body, CreateRequestDto::class.java)
                ?: problem(400, "Thiếu nội dung phiếu mượn")
            if (input.staffPhone.isBlank()) problem(400, "Thiếu số điện thoại nhân viên")
            if (input.quantity <= 0) problem(400, "Số lượng phải lớn hơn 0")
            val item = equipment.firstOrNull { it.id == input.equipmentId }
                ?: problem(404, "Không tìm thấy thiết bị")
            val available = availableStock(item)
            if (input.quantity > available) problem(409, "Chỉ còn $available ${item.unit} khả dụng")
            val created = BorrowRequestDto(
                id = nextRequestId++,
                staffPhone = input.staffPhone,
                equipmentId = item.id,
                equipmentName = item.name,
                equipmentCode = item.code,
                unit = item.unit,
                quantity = input.quantity,
                status = RequestStatus.PENDING_MANAGER.name,
                otp = null,
                createdAtEpochMillis = System.currentTimeMillis()
            )
            requests += created
            persistState()
            return 201 to created
        }

        val approve = Regex("/api/requests/(\\d+)/approve").matchEntire(path)
        if (method == "PATCH" && approve != null) {
            requireRole(actorRole, "MANAGER")
            val request = findRequest(approve.groupValues[1].toInt())
            if (request.status != RequestStatus.PENDING_MANAGER.name) problem(409, "Phiếu không còn ở trạng thái chờ duyệt")
            val updated = request.copy(status = RequestStatus.PENDING_STOREKEEPER.name)
            replace(updated)
            persistState()
            return 200 to updated
        }

        val issue = Regex("/api/requests/(\\d+)/issue").matchEntire(path)
        if (method == "POST" && issue != null) {
            requireRole(actorRole, "STOREKEEPER")
            val request = findRequest(issue.groupValues[1].toInt())
            if (request.status != RequestStatus.PENDING_STOREKEEPER.name) problem(409, "Phiếu không còn ở trạng thái chờ xuất kho")
            val item = equipment.firstOrNull { it.id == request.equipmentId }
                ?: problem(404, "Không tìm thấy thiết bị")
            val available = availableStock(item, excludingRequestId = request.id)
            if (request.quantity > available) problem(409, "Tồn khả dụng không đủ: còn $available ${item.unit}")
            val otp = random.nextInt(1_000_000).toString().padStart(6, '0')
            val updated = request.copy(status = RequestStatus.WAITING_CONFIRMATION.name, otp = otp)
            replace(updated)
            persistState()
            return 200 to IssueResponseDto(updated, otp)
        }

        val confirm = Regex("/api/requests/(\\d+)/confirm").matchEntire(path)
        if (method == "POST" && confirm != null) {
            requireRole(actorRole, "STAFF")
            val input = gson.fromJson(body, ConfirmReceiptDto::class.java)
                ?: problem(400, "Thiếu OTP")
            val request = findRequest(confirm.groupValues[1].toInt())
            if (request.staffPhone != input.staffPhone) problem(403, "Phiếu không thuộc nhân viên này")
            if (request.status != RequestStatus.WAITING_CONFIRMATION.name) problem(409, "Phiếu chưa sẵn sàng hoặc đã hoàn thành")
            if (request.otp != input.otp) problem(400, "OTP không chính xác")
            val itemIndex = equipment.indexOfFirst { it.id == request.equipmentId }
            if (itemIndex < 0) problem(404, "Không tìm thấy thiết bị")
            val item = equipment[itemIndex]
            if (item.stock < request.quantity) problem(409, "Tồn kho không đủ để hoàn tất phiếu")

            val updatedEquipment = item.copy(stock = item.stock - request.quantity)
            equipment[itemIndex] = updatedEquipment
            val updatedRequest = request.copy(status = RequestStatus.COMPLETED.name, otp = null)
            replace(updatedRequest)
            persistState()
            return 200 to ConfirmReceiptResponseDto(updatedRequest, updatedEquipment)
        }

        return 404 to ApiErrorDto("Không tìm thấy endpoint $method $path")
    }

    private fun requireRole(actual: String?, expected: String) {
        if (actual != expected) problem(403, "Endpoint này yêu cầu vai trò $expected")
    }

    private fun findRequest(id: Int): BorrowRequestDto =
        requests.firstOrNull { it.id == id } ?: problem(404, "Không tìm thấy phiếu")

    private fun replace(updated: BorrowRequestDto) {
        val index = requests.indexOfFirst { it.id == updated.id }
        if (index >= 0) requests[index] = updated
    }

    private fun availableStock(item: EquipmentDto, excludingRequestId: Int? = null): Int {
        val reserved = requests
            .filter {
                it.equipmentId == item.id &&
                    it.id != excludingRequestId &&
                    it.status != RequestStatus.COMPLETED.name
            }
            .sumOf { it.quantity }
        return (item.stock - reserved).coerceAtLeast(0)
    }

    private fun persistState() {
        preferences.edit()
            .putString("equipment", gson.toJson(equipment))
            .putString("requests", gson.toJson(requests))
            .apply()
    }

    private fun <T> loadList(key: String, type: java.lang.reflect.Type, default: List<T>): List<T> =
        preferences.getString(key, null)?.let { json ->
            runCatching { gson.fromJson<List<T>>(json, type) }.getOrNull()
        } ?: default

    private fun problem(statusCode: Int, message: String): Nothing = throw ApiProblem(statusCode, message)
    private class ApiProblem(val statusCode: Int, message: String) : RuntimeException(message)

    private companion object {
        const val TAG = "VNPT-FakeAPI"
        val initialEquipment = listOf(
            EquipmentDto(1, "Modem Quang iGate", "MDM-001", "Cái", 50),
            EquipmentDto(2, "Switch 8-Port", "SWT-008", "Cái", 20),
            EquipmentDto(3, "Kìm bấm mạng", "KIM-001", "Bộ", 10)
        )
        val demoAccounts = mapOf(
            "0912345678" to "STAFF",
            "0988888888" to "MANAGER",
            "0905111222" to "STOREKEEPER"
        )
    }
}
