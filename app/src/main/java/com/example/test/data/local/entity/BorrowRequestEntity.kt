package com.example.test.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.test.domain.model.BorrowRequest
import com.example.test.domain.model.RequestStatus

@Entity(
    tableName = "borrow_requests",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipmentId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["equipmentId"]),
        Index(value = ["staffPhone"]),
        Index(value = ["status"])
    ]
)
data class BorrowRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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

fun BorrowRequestEntity.toDomain() = BorrowRequest(
    id = id,
    staffPhone = staffPhone,
    equipmentId = equipmentId,
    equipmentName = equipmentName,
    equipmentCode = equipmentCode,
    unit = unit,
    quantity = quantity,
    status = status,
    otp = otp,
    createdAtEpochMillis = createdAtEpochMillis
)

fun BorrowRequest.toEntity() = BorrowRequestEntity(
    id = id,
    staffPhone = staffPhone,
    equipmentId = equipmentId,
    equipmentName = equipmentName,
    equipmentCode = equipmentCode,
    unit = unit,
    quantity = quantity,
    status = status,
    otp = otp,
    createdAtEpochMillis = createdAtEpochMillis
)
