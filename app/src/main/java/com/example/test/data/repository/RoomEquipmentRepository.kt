package com.example.test.data.repository

import androidx.room.withTransaction
import com.example.test.data.local.InventoryDatabase
import com.example.test.data.local.entity.toDomain
import com.example.test.data.local.entity.toEntity
import com.example.test.data.remote.ApiCallException
import com.example.test.data.remote.ConfirmReceiptDto
import com.example.test.data.remote.CreateRequestDto
import com.example.test.data.remote.InventoryApi
import com.example.test.data.remote.bodyOrThrow
import com.example.test.data.remote.toDomain
import com.example.test.domain.model.BorrowRequest
import com.example.test.domain.model.Equipment
import com.example.test.domain.model.OperationResult
import com.example.test.domain.repository.EquipmentRepository
import com.example.test.ui.model.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** API is the workflow authority; Room caches successful responses for the observable UI state. */
class RoomEquipmentRepository(
    private val database: InventoryDatabase,
    private val inventoryApi: InventoryApi
) : EquipmentRepository {
    private val equipmentDao = database.equipmentDao()
    private val requestDao = database.borrowRequestDao()

    override fun observeEquipment(): Flow<List<Equipment>> =
        equipmentDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeRequests(): Flow<List<BorrowRequest>> =
        requestDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun initialize() {
        // Fetch both snapshots before writing, so a failed request leaves the current cache intact.
        val equipment = inventoryApi.getEquipment().bodyOrThrow()
        val requests = inventoryApi.getRequests().bodyOrThrow()
        database.withTransaction {
            equipmentDao.upsertAll(equipment.map { it.toDomain().toEntity() })
            requestDao.deleteAll()
            requestDao.upsertAll(requests.map { it.toDomain().toEntity() })
        }
    }

    override suspend fun createRequest(
        actorRole: UserRole,
        staffPhone: String,
        equipmentId: Int,
        quantity: Int
    ): OperationResult = apiOperation {
        val request = inventoryApi.createRequest(
            actorRole = actorRole.name,
            request = CreateRequestDto(staffPhone, equipmentId, quantity)
        ).bodyOrThrow()
        cacheRequest(request.toDomain())
        OperationResult(true, "Đã tạo phiếu #${request.id}; đang chờ trưởng đơn vị duyệt")
    }

    override suspend fun approveRequest(
        actorRole: UserRole,
        requestId: Int
    ): OperationResult = apiOperation {
        val request = inventoryApi.approveRequest(actorRole.name, requestId).bodyOrThrow()
        cacheRequest(request.toDomain())
        OperationResult(true, "Đã duyệt phiếu #${request.id}")
    }

    override suspend fun prepareIssue(
        actorRole: UserRole,
        requestId: Int
    ): OperationResult = apiOperation {
        val response = inventoryApi.issueRequest(actorRole.name, requestId).bodyOrThrow()
        cacheRequest(response.request.toDomain())
        OperationResult(
            true,
            "Đã chuẩn bị phiếu #${response.request.id}; OTP bàn giao: ${response.handoverOtp}"
        )
    }

    override suspend fun confirmReceipt(
        actorRole: UserRole,
        requestId: Int,
        staffPhone: String,
        otp: String
    ): OperationResult = apiOperation {
        val response = inventoryApi.confirmReceipt(
            actorRole = actorRole.name,
            requestId = requestId,
            request = ConfirmReceiptDto(staffPhone, otp)
        ).bodyOrThrow()
        database.withTransaction {
            equipmentDao.upsertAll(listOf(response.equipment.toDomain().toEntity()))
            requestDao.upsert(response.request.toDomain().toEntity())
        }
        OperationResult(true, "Đã xác nhận nhận hàng; phiếu #${response.request.id} hoàn thành")
    }

    private suspend fun cacheRequest(request: BorrowRequest) {
        database.withTransaction { requestDao.upsert(request.toEntity()) }
    }

    private suspend fun apiOperation(action: suspend () -> OperationResult): OperationResult = try {
        action()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        val message = when (error) {
            is ApiCallException -> error.message
            else -> "Không kết nối được API: ${error.message.orEmpty()}"
        }
        OperationResult(false, message ?: "Yêu cầu API thất bại")
    }
}
