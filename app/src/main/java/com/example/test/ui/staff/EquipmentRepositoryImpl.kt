package com.example.test.ui.staff

import com.example.test.ui.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

/** In-memory data source shared by all role screens for the demo workflow. */
class EquipmentRepositoryImpl : EquipmentRepository {
    private val mutex = Mutex()

    private val mutableEquipment = MutableStateFlow(
        listOf(
            Equipment(1, "Modem Quang iGate", "MDM-001", "Cái", 50),
            Equipment(2, "Switch 8-Port", "SWT-008", "Cái", 20),
            Equipment(3, "Kìm bấm mạng", "KIM-001", "Bộ", 10)
        )
    )
    override val equipment = mutableEquipment.asStateFlow()

    private val mutableRequests = MutableStateFlow<List<BorrowRequest>>(emptyList())
    override val requests = mutableRequests.asStateFlow()

    private var nextRequestId = 1

    override suspend fun createRequest(
        actorRole: UserRole,
        staffPhone: String,
        equipmentId: Int,
        quantity: Int
    ): OperationResult = mutex.withLock {
        if (actorRole != UserRole.STAFF) return@withLock failure("Chỉ nhân viên được tạo phiếu")
        if (quantity <= 0) return@withLock failure("Số lượng phải lớn hơn 0")

        val item = mutableEquipment.value.firstOrNull { it.id == equipmentId }
            ?: return@withLock failure("Không tìm thấy thiết bị")
        val available = availableStock(item)
        if (quantity > available) {
            return@withLock failure("Chỉ còn $available ${item.unit} khả dụng")
        }

        val request = BorrowRequest(
            id = nextRequestId++,
            staffPhone = staffPhone,
            equipmentId = item.id,
            equipmentName = item.name,
            equipmentCode = item.code,
            unit = item.unit,
            quantity = quantity,
            status = RequestStatus.PENDING_MANAGER
        )
        mutableRequests.value = mutableRequests.value + request
        success("Đã tạo phiếu #${request.id}, đang chờ trưởng đơn vị duyệt")
    }

    override suspend fun approveRequest(
        actorRole: UserRole,
        requestId: Int
    ): OperationResult = mutex.withLock {
        if (actorRole != UserRole.MANAGER) return@withLock failure("Chỉ trưởng đơn vị được duyệt phiếu")
        val request = mutableRequests.value.firstOrNull { it.id == requestId }
            ?: return@withLock failure("Không tìm thấy phiếu")
        if (request.status != RequestStatus.PENDING_MANAGER) {
            return@withLock failure("Phiếu không còn ở trạng thái chờ duyệt")
        }
        updateRequest(request.copy(status = RequestStatus.PENDING_STOREKEEPER))
        success("Đã duyệt phiếu #${request.id}")
    }

    override suspend fun prepareIssue(
        actorRole: UserRole,
        requestId: Int
    ): OperationResult = mutex.withLock {
        if (actorRole != UserRole.STOREKEEPER) return@withLock failure("Chỉ thủ kho được xuất thiết bị")
        val request = mutableRequests.value.firstOrNull { it.id == requestId }
            ?: return@withLock failure("Không tìm thấy phiếu")
        if (request.status != RequestStatus.PENDING_STOREKEEPER) {
            return@withLock failure("Phiếu không còn ở trạng thái chờ xuất kho")
        }

        val item = mutableEquipment.value.firstOrNull { it.id == request.equipmentId }
            ?: return@withLock failure("Không tìm thấy thiết bị")
        val available = availableStock(item)
        if (request.quantity > available) {
            return@withLock failure("Tồn khả dụng không đủ: còn $available ${item.unit}")
        }

        val otp = Random.nextInt(0, 1_000_000).toString().padStart(6, '0')
        updateRequest(request.copy(status = RequestStatus.WAITING_CONFIRMATION, otp = otp))
        success("Đã chuẩn bị phiếu #${request.id}; OTP bàn giao: $otp")
    }

    override suspend fun confirmReceipt(
        actorRole: UserRole,
        requestId: Int,
        staffPhone: String,
        otp: String
    ): OperationResult = mutex.withLock {
        if (actorRole != UserRole.STAFF) return@withLock failure("Chỉ nhân viên được xác nhận nhận hàng")
        val request = mutableRequests.value.firstOrNull { it.id == requestId }
            ?: return@withLock failure("Không tìm thấy phiếu")
        if (request.staffPhone != staffPhone) return@withLock failure("Phiếu không thuộc nhân viên này")
        if (request.status != RequestStatus.WAITING_CONFIRMATION) {
            return@withLock failure("Phiếu chưa sẵn sàng xác nhận hoặc đã hoàn thành")
        }
        if (request.otp != otp) return@withLock failure("OTP không chính xác")

        val index = mutableEquipment.value.indexOfFirst { it.id == request.equipmentId }
        if (index < 0) return@withLock failure("Không tìm thấy thiết bị")
        val item = mutableEquipment.value[index]
        if (item.stock < request.quantity) return@withLock failure("Tồn kho không đủ để hoàn tất phiếu")

        mutableEquipment.value = mutableEquipment.value.toMutableList().also {
            it[index] = item.copy(stock = item.stock - request.quantity)
        }
        updateRequest(request.copy(status = RequestStatus.COMPLETED, otp = null))
        success("Đã xác nhận nhận hàng; phiếu #${request.id} hoàn thành")
    }

    private fun availableStock(item: Equipment): Int {
        val reserved = mutableRequests.value
            .filter {
                it.equipmentId == item.id && it.status == RequestStatus.WAITING_CONFIRMATION
            }
            .sumOf { it.quantity }
        return (item.stock - reserved).coerceAtLeast(0)
    }

    private fun updateRequest(updated: BorrowRequest) {
        mutableRequests.value = mutableRequests.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    private fun success(message: String) = OperationResult(true, message)
    private fun failure(message: String) = OperationResult(false, message)
}
