package com.example.test.ui.Login.repository

import com.example.test.ui.model.UserRole

interface AuthLogin {
    suspend fun login(phone:String ,password: String): UserRole?
}