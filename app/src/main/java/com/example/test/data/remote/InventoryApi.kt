package com.example.test.data.remote

import retrofit2.http.GET

/** Retrofit contract. The current base URL points to the local fake HTTP server. */
interface InventoryApi {
    @GET("equipment")
    suspend fun getEquipment(): List<EquipmentDto>
}

data class EquipmentDto(
    val id: Int,
    val name: String,
    val code: String,
    val unit: String,
    val stock: Int
)
