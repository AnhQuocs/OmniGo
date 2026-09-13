package com.example.omnigo.features.main

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.omnigo.BaseComponentActivity
import com.example.omnigo.features.auth.presentation.ui.login.LoginScreen
import com.example.omnigo.features.auth.presentation.ui.register.RegisterScreen
import com.example.omnigo.features.main.presentation.ui.MainCustomerScreen
import com.example.omnigo.features.main.presentation.viewmodel.AuthState
import com.example.omnigo.features.main.presentation.viewmodel.MainViewModel
import com.example.omnigo.ui.theme.BackgroundLight
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

            val mainViewModel: MainViewModel = hiltViewModel()
            val authState by mainViewModel.authState.collectAsStateWithLifecycle()

            // Khi đang đọc phiên từ DataStore, giữ màn hình nền
            if (authState == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundLight)
                )
                return@setOmniGoContent
            }

            val startDestination = if (authState is AuthState.Authenticated) {
                "main_customer"
            } else {
                "login"
            }

            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable("login") {
                    LoginScreen(
                        onNavigateToRegister = {
                            navController.navigate("register")
                        },
                        onLoginSuccess = { user ->
                            navController.navigate("main_customer") {
                                popUpTo("login") { inclusive = true }
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
                            navController.navigate("main_customer") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    )
                }

                composable("main_customer") {
                    MainCustomerScreen(
                        onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
