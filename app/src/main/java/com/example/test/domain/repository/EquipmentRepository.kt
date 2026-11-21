package com.example.test.domain.repository

import com.example.test.domain.model.BorrowRequest
import com.example.test.domain.model.Equipment
import com.example.test.domain.model.OperationResult
import com.example.test.ui.model.UserRole
import kotlinx.coroutines.flow.Flow

interface EquipmentRepository {
    fun observeEquipment(): Flow<List<Equipment>>
    fun observeRequests(): Flow<List<BorrowRequest>>

    suspend fun initialize()
    suspend fun createRequest(
        actorRole: UserRole,
        staffPhone: String,
        equipmentId: Int,
        quantity: Int
    ): OperationResult

    suspend fun approveRequest(actorRole: UserRole, requestId: Int): OperationResult
    suspend fun prepareIssue(actorRole: UserRole, requestId: Int): OperationResult
    suspend fun confirmReceipt(
        actorRole: UserRole,
        requestId: Int,
        staffPhone: String,
        otp: String
    ): OperationResult
}
