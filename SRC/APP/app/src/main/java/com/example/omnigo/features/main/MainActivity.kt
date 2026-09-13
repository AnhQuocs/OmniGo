package com.example.omnigo.features.main

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.omnigo.BaseComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.omnigo.features.auth.presentation.ui.login.LoginScreen
import com.example.omnigo.features.auth.presentation.ui.register.RegisterScreen
import com.example.omnigo.features.main.presentation.ui.MainCustomerScreen
import com.example.omnigo.features.main.presentation.ui.MainDriverScreen
import com.example.omnigo.features.main.presentation.ui.SplashScreen
import com.example.omnigo.features.onboarding.presentation.ui.OnboardingScreen
import com.example.omnigo.ui.theme.BackgroundLight
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val goToAuth = intent.getBooleanExtra("GO_TO_AUTH", false)

        setOmniGoContent {
            val systemBarColorArgb = BackgroundLight.toArgb()

            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.light(
                        scrim = systemBarColorArgb,
                        darkScrim = systemBarColorArgb
                    ),
                    navigationBarStyle = SystemBarStyle.light(
                        scrim = systemBarColorArgb,
                        darkScrim = systemBarColorArgb
                    )
                )
            }

            MainApp(goToAuth = goToAuth)
        }
    }
}

@Composable
fun MainApp(goToAuth: Boolean) {
    val navController = rememberNavController()
    val startRoute = if (goToAuth) "auth_root" else "splash_root"

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        splashGraph(navController)
        onboardingGraph(navController)
        authGraph(navController)
        customerGraph(navController)
        driverGraph(navController)
    }
}

fun NavGraphBuilder.splashGraph(navController: NavController) {
    navigation(
        startDestination = "splash",
        route = "splash_root"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToDestination = { targetDestination ->
                    navController.navigate(targetDestination) {
                        popUpTo("splash_root") { inclusive = true }
                    }
                }
            )
        }
    }
}

fun NavGraphBuilder.onboardingGraph(navController: NavController) {
    navigation(
        startDestination = "onboarding",
        route = "onboarding_root"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToAuth = {
                    navController.navigate("auth_root") {
                        popUpTo("onboarding_root") { inclusive = true }
                    }
                }
            )
        }
    }
}

fun NavGraphBuilder.authGraph(navController: NavController) {
    navigation(
        startDestination = "login",
        route = "auth_root"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = { user ->
                    val target = if (user.role.name.equals("DRIVER", ignoreCase = true)) {
                        "driver_root"
                    } else {
                        "customer_root"
                    }
                    navController.navigate(target) {
                        popUpTo("auth_root") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { user ->
                    val target = if (user.role.name.equals("DRIVER", ignoreCase = true)) {
                        "driver_root"
                    } else {
                        "customer_root"
                    }
                    navController.navigate(target) {
                        popUpTo("auth_root") { inclusive = true }
                    }
                }
            )
        }
    }
}

fun NavGraphBuilder.customerGraph(navController: NavController) {
    navigation(
        startDestination = "main_customer",
        route = "customer_root"
    ) {
        composable("main_customer") {
            MainCustomerScreen(
                onNavigateToLogin = {
                    navController.navigate("auth_root") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

fun NavGraphBuilder.driverGraph(navController: NavController) {
    navigation(
        startDestination = "main_driver",
        route = "driver_root"
    ) {
        composable("main_driver") {
            MainDriverScreen(
                onNavigateToLogin = {
                    navController.navigate("auth_root") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}