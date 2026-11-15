package com.example.test.ui.Login.repository

import com.example.test.ui.model.UserRole

class AuthLoginImpl : AuthLogin {
    override suspend fun login(
        phone: String,
        password: String
    ): UserRole? {
        return when (phone) {
            AuthLoginProvider.PHONE_STAFF -> if (password == AuthLoginProvider.PASS_STAFF) UserRole.STAFF else null
            AuthLoginProvider.PHONE_MANAGER -> if (password == AuthLoginProvider.PASS_MANAGER) UserRole.MANAGER else null
            AuthLoginProvider.PHONE_STOREKEEPER -> if (password == AuthLoginProvider.PASS_STOREKEEPER) UserRole.STOREKEEPER else null
            else -> null
        }
    }
}