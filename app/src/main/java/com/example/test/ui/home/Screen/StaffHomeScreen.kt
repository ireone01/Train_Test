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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.domain.model.BorrowRequest
import com.example.test.domain.model.Equipment
import com.example.test.domain.model.RequestStatus
import com.example.test.ui.staff.BorrowRequestCard
import com.example.test.ui.staff.BorrowRequestViewModel
import com.example.test.ui.staff.EquipmentListContent
import com.example.test.ui.staff.EquipmentViewModel

@Composable
fun StaffHomeScreen(
    staffPhone: String,
    onLogout: () -> Unit,
    onNotificationClick: () -> Unit = {},
    equipmentViewModel: EquipmentViewModel = viewModel(),
    requestViewModel: BorrowRequestViewModel = viewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedEquipment by remember { mutableStateOf<Equipment?>(null) }
    var selectedRequestForOtp by remember { mutableStateOf<BorrowRequest?>(null) }

    val requests by requestViewModel.requests.collectAsState()
    val message by requestViewModel.message.collectAsState()

    LaunchedEffect(requests, selectedRequestForOtp?.id) {
        val selectedId = selectedRequestForOtp?.id
        if (selectedId != null && requests.any {
                it.id == selectedId && it.status == RequestStatus.COMPLETED
            }
        ) {
            selectedRequestForOtp = null
        }
    }

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
                Text("Nhân viên")
                Row {
                    TextButton(onClick = onNotificationClick) { Text("Nhắc việc") }
                    TextButton(onClick = onLogout) { Text("Đăng xuất") }
                }
            }
            message?.let { Text(it, modifier = Modifier.padding(horizontal = 16.dp)) }

            SecondaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Thiết bị") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Phiếu của tôi") }
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> EquipmentListContent(
                        viewModel = equipmentViewModel,
                        onBorrowRequest = { selectedEquipment = it }
                    )
                    1 -> MyRequestsContent(
                        requests = requests.filter { it.staffPhone == staffPhone },
                        onConfirm = { selectedRequestForOtp = it }
                    )
                }
            }
        }
    }

    selectedEquipment?.let { equipment ->
        BorrowEquipmentDialog(
            equipment = equipment,
            onDismiss = { selectedEquipment = null },
            onSubmit = { quantity ->
                requestViewModel.createRequest(staffPhone, equipment.id, quantity)
                selectedEquipment = null
            }
        )
    }

    selectedRequestForOtp?.let { request ->
        ConfirmOtpDialog(
            request = request,
            message = message,
            onDismiss = { selectedRequestForOtp = null },
            onConfirm = { otp -> requestViewModel.confirmReceipt(request.id, staffPhone, otp) }
        )
    }
}

@Composable
private fun MyRequestsContent(
    requests: List<BorrowRequest>,
    onConfirm: (BorrowRequest) -> Unit
) {
    if (requests.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bạn chưa có phiếu mượn")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests, key = { it.id }) { request ->
                BorrowRequestCard(
                    request = request,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    actionLabel = if (request.status == RequestStatus.WAITING_CONFIRMATION) {
                        "Nhập OTP xác nhận"
                    } else null,
                    onAction = if (request.status == RequestStatus.WAITING_CONFIRMATION) {
                        { onConfirm(request) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun BorrowEquipmentDialog(
    equipment: Equipment,
    onDismiss: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    var quantityText by rememberSaveable(equipment.id) { mutableStateOf("1") }
    val quantity = quantityText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo phiếu mượn") },
        text = {
            Column {
                Text("${equipment.name} • Tồn kho: ${equipment.stock} ${equipment.unit}")
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter(Char::isDigit).take(6) },
                    label = { Text("Số lượng") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { quantity?.let(onSubmit) },
                enabled = quantity != null && quantity > 0 && quantity <= equipment.stock
            ) { Text("Tạo phiếu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun ConfirmOtpDialog(
    request: BorrowRequest,
    message: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var otp by rememberSaveable(request.id) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận nhận hàng • Phiếu #${request.id}") },
        text = {
            Column {
                Text("Nhập mã OTP do thủ kho bàn giao.")
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                    label = { Text("OTP 6 chữ số") },
                    singleLine = true
                )
                message?.let { Text(it) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(otp) }, enabled = otp.length == 6) {
                Text("Xác nhận")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}

@Preview(showBackground = true)
@Composable
private fun StaffHomePreview() {
    StaffHomeScreen(staffPhone = "0912345678", onLogout = {})
}
