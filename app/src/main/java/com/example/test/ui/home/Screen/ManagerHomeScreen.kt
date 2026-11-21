package com.example.test.ui.home.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
fun ManagerHomeScreen(
    onLogout: () -> Unit,
    requestViewModel: BorrowRequestViewModel = viewModel()
) {
    val requests by requestViewModel.requests.collectAsState()
    val message by requestViewModel.message.collectAsState()
    val pendingRequests = requests.filter { it.status == RequestStatus.PENDING_MANAGER }

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
                Text("Trưởng đơn vị — Phiếu chờ duyệt")
                TextButton(onClick = onLogout) { Text("Đăng xuất") }
            }
            message?.let { Text(it, modifier = Modifier.padding(horizontal = 16.dp)) }

            if (pendingRequests.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Không có phiếu chờ duyệt")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pendingRequests, key = { it.id }) { request ->
                        BorrowRequestCard(
                            request = request,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            actionLabel = "Phê duyệt",
                            onAction = { requestViewModel.approveRequest(request.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ManagerHomePreview() {
    ManagerHomeScreen(onLogout = {})
}
