package com.example.test.data.remote

/** Contract for the inventory service. A Retrofit implementation can replace the fake one. */
interface InventoryApi {
    suspend fun getEquipment(): List<EquipmentDto>
}

data class EquipmentDto(
    val id: Int,
    val name: String,
    val code: String,
    val unit: String,
    val stock: Int
)
