package com.example.test.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Example REST contract. The loopback mock server implements these routes for this exercise. */
interface InventoryApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @GET("equipment")
    suspend fun getEquipment(): Response<List<EquipmentDto>>

    @GET("requests")
    suspend fun getRequests(): Response<List<BorrowRequestDto>>

    @POST("requests")
    suspend fun createRequest(
        @Header("X-Actor-Role") actorRole: String,
        @Body request: CreateRequestDto
    ): Response<BorrowRequestDto>

    @PATCH("requests/{id}/approve")
    suspend fun approveRequest(
        @Header("X-Actor-Role") actorRole: String,
        @Path("id") requestId: Int
    ): Response<BorrowRequestDto>

    @POST("requests/{id}/issue")
    suspend fun issueRequest(
        @Header("X-Actor-Role") actorRole: String,
        @Path("id") requestId: Int
    ): Response<IssueResponseDto>

    @POST("requests/{id}/confirm")
    suspend fun confirmReceipt(
        @Header("X-Actor-Role") actorRole: String,
        @Path("id") requestId: Int,
        @Body request: ConfirmReceiptDto
    ): Response<ConfirmReceiptResponseDto>
}
