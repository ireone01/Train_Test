package com.example.test.ui.staff

interface EquipmentRepository {
    suspend fun getAll(): List<Equipment>
}