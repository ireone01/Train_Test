package com.example.test.ui.Route

import com.example.test.ui.model.UserRole

object AppRoute {
    const val SplashScreen = "splash"
    const val Login = "login"
    const val Home = "home/{role}/{phone}"

    fun home(role: UserRole, phone: String) = "home/${role.name}/$phone"
}
