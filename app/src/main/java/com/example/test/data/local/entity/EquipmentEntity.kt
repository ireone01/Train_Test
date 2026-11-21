package com.example.test.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.test.domain.model.Equipment

@Entity(
    tableName = "equipment",
    indices = [Index(value = ["code"], unique = true)]
)
data class EquipmentEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val code: String,
    val unit: String,
    val stock: Int
)

fun EquipmentEntity.toDomain() = Equipment(
    id = id,
    name = name,
    code = code,
    unit = unit,
    stock = stock
)

fun Equipment.toEntity() = EquipmentEntity(
    id = id,
    name = name,
    code = code,
    unit = unit,
    stock = stock
)
