package com.example.test.ui.home

import androidx.compose.runtime.Composable
import com.example.test.ui.home.Screen.ManagerHomeScreen
import com.example.test.ui.home.Screen.StaffHomeScreen
import com.example.test.ui.home.Screen.StoreKeeperHomeScreen
import com.example.test.ui.model.UserRole


@Composable
fun HomeRoute(userRole: UserRole, staffPhone: String, onLogout: () -> Unit) {

    when (userRole) {
        UserRole.STAFF -> StaffHomeScreen(staffPhone = staffPhone, onLogout = onLogout)
        UserRole.MANAGER -> ManagerHomeScreen(onLogout = onLogout)
        UserRole.STOREKEEPER -> StoreKeeperHomeScreen(onLogout = onLogout)
    }
}
