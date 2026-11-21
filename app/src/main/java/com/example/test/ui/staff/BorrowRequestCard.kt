package com.example.test.ui.staff

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BorrowRequestCard(
    request: BorrowRequest,
    modifier: Modifier = Modifier,
    inventoryStock: Int? = null,
    showOtp: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Phiếu #${request.id} • ${request.status.label}")
            Text("${request.equipmentName} (${request.equipmentCode})")
            Text("Số lượng: ${request.quantity} ${request.unit}")
            Text("Nhân viên: ${request.staffPhone}")
            inventoryStock?.let { Text("Tồn kho hiện tại: $it ${request.unit}") }
            if (showOtp && request.otp != null) {
                Text("OTP bàn giao: ${request.otp}")
            }
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
