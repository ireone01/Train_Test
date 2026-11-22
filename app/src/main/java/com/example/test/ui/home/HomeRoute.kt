package com.example.test.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.example.test.ui.home.Screen.NotificationAlarmDialog
import com.example.test.ui.home.Screen.ManagerHomeScreen
import com.example.test.ui.home.Screen.StaffHomeScreen
import com.example.test.ui.home.Screen.StoreKeeperHomeScreen
import com.example.test.ui.model.UserRole


@Composable
fun HomeRoute(userRole: UserRole, staffPhone: String, onLogout: () -> Unit) {
    var showNotificationDialog by remember { mutableStateOf(false) }

    Box {
        when (userRole) {
            UserRole.STAFF -> StaffHomeScreen(
                staffPhone = staffPhone,
                onLogout = onLogout,
                onNotificationClick = { showNotificationDialog = true }
            )
            UserRole.MANAGER -> ManagerHomeScreen(
                onLogout = onLogout,
                onNotificationClick = { showNotificationDialog = true }
            )
            UserRole.STOREKEEPER -> StoreKeeperHomeScreen(
                onLogout = onLogout,
                onNotificationClick = { showNotificationDialog = true }
            )
        }
        if (showNotificationDialog) {
            NotificationAlarmDialog(onDismiss = { showNotificationDialog = false })
        }
    }
}
