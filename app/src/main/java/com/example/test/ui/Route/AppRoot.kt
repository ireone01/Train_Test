package com.example.test.ui.Route

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.ui.Login.LoginScreen
import com.example.test.ui.Splash.SplashScreen

@Composable
fun AppRoot() {

    fun NavHostController.navigateTo(
        route: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = false
    ) {
        navigate(route) {
            launchSingleTop = true

            if (popUpToRoute != null) {
                popUpTo(popUpToRoute) {
                    this.inclusive = inclusive
                }
            }
        }
    }

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoute.SplashScreen) {
        composable(AppRoute.SplashScreen) {
            SplashScreen(
                onFinished = {
                    navController.navigateTo(
                        route = AppRoute.Login,
                        popUpToRoute = AppRoute.SplashScreen,
                        inclusive = true
                    )
                }
            )
        }

        composable(AppRoute.Login) { LoginScreen() }

    }
}