package com.example.test.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.di.AppContainer
import com.example.test.domain.model.BorrowRequest
import com.example.test.domain.model.Equipment
import com.example.test.domain.model.OperationResult
import com.example.test.domain.repository.EquipmentRepository
import com.example.test.ui.model.UserRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BorrowRequestViewModel(
    private val repository: EquipmentRepository = AppContainer.equipmentRepository
) : ViewModel() {
    val requests = repository.observeRequests().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList<BorrowRequest>()
    )
    val equipment = repository.observeEquipment().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList<Equipment>()
    )

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun createRequest(staffPhone: String, equipmentId: Int, quantity: Int) {
        perform {
            repository.createRequest(UserRole.STAFF, staffPhone, equipmentId, quantity)
        }
    }

    fun approveRequest(requestId: Int) {
        perform { repository.approveRequest(UserRole.MANAGER, requestId) }
    }

    fun prepareIssue(requestId: Int) {
        perform { repository.prepareIssue(UserRole.STOREKEEPER, requestId) }
    }

    fun confirmReceipt(requestId: Int, staffPhone: String, otp: String) {
        perform {
            repository.confirmReceipt(
                actorRole = UserRole.STAFF,
                requestId = requestId,
                staffPhone = staffPhone,
                otp = otp
            )
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun perform(action: suspend () -> OperationResult) {
        viewModelScope.launch {
            _message.value = action().message
        }
    }
}
