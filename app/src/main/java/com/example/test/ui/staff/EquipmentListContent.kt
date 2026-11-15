package com.example.test.ui.staff

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun EquipmentListContent(viewModel: EquipmentViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val equipment by viewModel.filteredEquipment.collectAsState()

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onSearchQueryChanges,
            label = { Text("Tìm thiết bị") },
            modifier = Modifier.fillMaxWidth()
        )

        if (equipment.isEmpty()) {
            Text("Không tìm thấy thiết bị")
        } else {
            LazyColumn {
                items(equipment, key = { it.id }) { item ->
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.name)
                        Text("Mã: ${item.code}")
                        Text("Tồn kho: ${item.stock} ${item.unit}")
                    }
                }
            }
        }
    }
}