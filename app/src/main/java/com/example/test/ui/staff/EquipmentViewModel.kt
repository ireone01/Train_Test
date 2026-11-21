package com.example.test.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.di.AppContainer
import com.example.test.domain.repository.EquipmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class EquipmentViewModel(private val repository: EquipmentRepository = AppContainer.equipmentRepository) :
    ViewModel() {
    private val _searchQuery = MutableStateFlow("")

    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChanges(query: String) {
        _searchQuery.value = query
    }

    val filteredEquipment = combine(repository.observeEquipment(), _searchQuery) { items, query ->
        if (query.isBlank()) items
        else items.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

}
