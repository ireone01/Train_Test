package com.example.test.ui.Route

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.ui.Login.LoginScreen
import com.example.test.ui.Splash.SplashScreen
import com.example.test.ui.home.HomeRoute
import com.example.test.ui.model.UserRole

@Composable
fun AppRoot() {

    fun NavHostController.navigateTo(
        route: String,
        popupToRoute: String? = null,
        inclusive: Boolean = false
    ) {
        navigate(route) {
            launchSingleTop = true
            if (popupToRoute != null) {
                popUpTo(popupToRoute) {
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
                        popupToRoute = AppRoute.SplashScreen,
                        inclusive = true
                    )
                }
            )
        }

        composable(AppRoute.Login) {
            LoginScreen(onLoginSuccess = { role ->
                navController.navigateTo(
                    AppRoute.home(role),
                    popupToRoute = AppRoute.Login, true
                )
            }, viewModel())
        }

        composable(AppRoute.Home) { backstackEntry ->
            val roleName = backstackEntry.arguments?.getString("role")
            val role = roleName?.let { UserRole.valueOf(it) }
            if (role != null) HomeRoute(role)
        }
    }
}