package com.example.test.domain.model

data class BorrowRequest(
    val id: Int,
    val staffPhone: String,
    val equipmentId: Int,
    val equipmentName: String,
    val equipmentCode: String,
    val unit: String,
    val quantity: Int,
    val status: RequestStatus,
    val otp: String? = null,
    val createdAtEpochMillis: Long
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
