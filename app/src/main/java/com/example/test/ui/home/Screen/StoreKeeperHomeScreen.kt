package com.example.test.ui.home.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.domain.model.RequestStatus
import com.example.test.ui.staff.BorrowRequestCard
import com.example.test.ui.staff.BorrowRequestViewModel

@Composable
fun StoreKeeperHomeScreen(
    onLogout: () -> Unit,
    onNotificationClick: () -> Unit = {},
    requestViewModel: BorrowRequestViewModel = viewModel()
) {
    val requests by requestViewModel.requests.collectAsState()
    val equipment by requestViewModel.equipment.collectAsState()
    val message by requestViewModel.message.collectAsState()
    val pendingIssue = requests.filter { it.status == RequestStatus.PENDING_STOREKEEPER }
    val waitingHandover = requests.filter { it.status == RequestStatus.WAITING_CONFIRMATION }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Thủ kho — Xuất thiết bị")
                Row {
                    TextButton(onClick = onNotificationClick) { Text("Nhắc việc") }
                    TextButton(onClick = onLogout) { Text("Đăng xuất") }
                }
            }
            message?.let { Text(it, modifier = Modifier.padding(horizontal = 16.dp)) }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Text("Phiếu chờ xuất kho", modifier = Modifier.padding(16.dp)) }
                if (pendingIssue.isEmpty()) {
                    item { Text("Không có phiếu chờ xuất", modifier = Modifier.padding(horizontal = 16.dp)) }
                } else {
                    items(pendingIssue, key = { "issue-${it.id}" }) { request ->
                        val stock = equipment.firstOrNull { it.id == request.equipmentId }?.stock
                        BorrowRequestCard(
                            request = request,
                            inventoryStock = stock,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            actionLabel = "Xuất kho và tạo OTP",
                            onAction = { requestViewModel.prepareIssue(request.id) }
                        )
                    }
                }

                item { Text("OTP chờ bàn giao", modifier = Modifier.padding(16.dp)) }
                if (waitingHandover.isEmpty()) {
                    item { Text("Chưa có OTP cần bàn giao", modifier = Modifier.padding(horizontal = 16.dp)) }
                } else {
                    items(waitingHandover, key = { "handover-${it.id}" }) { request ->
                        BorrowRequestCard(
                            request = request,
                            showOtp = true,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StoreKeeperHomePreview() {
    StoreKeeperHomeScreen(onLogout = {})
}
