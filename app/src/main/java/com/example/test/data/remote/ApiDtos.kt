package com.example.test.data.remote

import com.example.test.domain.model.BorrowRequest
import com.example.test.domain.model.Equipment
import com.example.test.domain.model.RequestStatus

data class LoginRequestDto(val phone: String, val password: String)
data class LoginResponseDto(val phone: String, val role: String, val accessToken: String)

data class EquipmentDto(
    val id: Int,
    val name: String,
    val code: String,
    val unit: String,
    val stock: Int
)

data class CreateRequestDto(val staffPhone: String, val equipmentId: Int, val quantity: Int)
data class ConfirmReceiptDto(val staffPhone: String, val otp: String)
data class ApiErrorDto(val message: String)

data class BorrowRequestDto(
    val id: Int,
    val staffPhone: String,
    val equipmentId: Int,
    val equipmentName: String,
    val equipmentCode: String,
    val unit: String,
    val quantity: Int,
    val status: String,
    val otp: String?,
    val createdAtEpochMillis: Long
)

data class IssueResponseDto(val request: BorrowRequestDto, val handoverOtp: String)
data class ConfirmReceiptResponseDto(val request: BorrowRequestDto, val equipment: EquipmentDto)

fun EquipmentDto.toDomain() = Equipment(
    id = id,
    name = name,
    code = code,
    unit = unit,
    stock = stock
)

fun BorrowRequestDto.toDomain() = BorrowRequest(
    id = id,
    staffPhone = staffPhone,
    equipmentId = equipmentId,
    equipmentName = equipmentName,
    equipmentCode = equipmentCode,
    unit = unit,
    quantity = quantity,
    status = RequestStatus.valueOf(status),
    otp = otp,
    createdAtEpochMillis = createdAtEpochMillis
)
