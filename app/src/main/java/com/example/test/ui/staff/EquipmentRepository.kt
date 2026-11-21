package com.example.test.ui.staff

import com.example.test.ui.model.UserRole
import kotlinx.coroutines.flow.StateFlow

data class BorrowRequest(
    val id: Int,
    val staffPhone: String,
    val equipmentId: Int,
    val equipmentName: String,
    val equipmentCode: String,
    val unit: String,
    val quantity: Int,
    val status: RequestStatus,
    val otp: String? = null
)

enum class RequestStatus(val label: String) {
    PENDING_MANAGER("Chờ trưởng đơn vị duyệt"),
    PENDING_STOREKEEPER("Chờ thủ kho xuất"),
    WAITING_CONFIRMATION("Chờ nhân viên xác nhận"),
    COMPLETED("Đã hoàn thành")
}

data class OperationResult(
    val succeeded: Boolean,
    val message: String
)

interface EquipmentRepository {
    val equipment: StateFlow<List<Equipment>>
    val requests: StateFlow<List<BorrowRequest>>

    suspend fun createRequest(
        actorRole: UserRole,
        staffPhone: String,
        equipmentId: Int,
        quantity: Int
    ): OperationResult

    suspend fun approveRequest(actorRole: UserRole, requestId: Int): OperationResult
    suspend fun prepareIssue(actorRole: UserRole, requestId: Int): OperationResult
    suspend fun confirmReceipt(
        actorRole: UserRole,
        requestId: Int,
        staffPhone: String,
        otp: String
    ): OperationResult
}
