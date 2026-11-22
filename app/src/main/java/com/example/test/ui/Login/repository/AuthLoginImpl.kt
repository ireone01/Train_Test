package com.example.test.ui.Login.repository

import com.example.test.data.remote.InventoryApi
import com.example.test.data.remote.LoginRequestDto
import com.example.test.data.remote.NetworkModule
import com.example.test.data.remote.bodyOrThrow
import com.example.test.ui.model.UserRole
import kotlinx.coroutines.CancellationException

class AuthLoginImpl(
    private val inventoryApi: InventoryApi = NetworkModule.api
) : AuthLogin {
    override suspend fun login(
        phone: String,
        password: String
    ): UserRole? {
        return try {
            val response = inventoryApi.login(LoginRequestDto(phone, password)).bodyOrThrow()
            UserRole.valueOf(response.role)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
    }
}
