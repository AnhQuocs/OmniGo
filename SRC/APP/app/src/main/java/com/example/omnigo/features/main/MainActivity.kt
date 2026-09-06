package com.example.omnigo.features.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.omnigo.BaseComponentActivity
import com.example.omnigo.features.auth.presentation.ui.login.LoginScreen
import com.example.omnigo.features.auth.presentation.ui.register.RegisterScreen
import com.example.omnigo.ui.theme.OmniGoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OmniGoTheme(darkTheme = false) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onNavigateToRegister = {
                                navController.navigate("register")
                            },
                            onLoginSuccess = { user ->
                                // TODO: Handle navigation after login success
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onNavigateToLogin = {
                                navController.popBackStack()
                            },
                            onRegisterSuccess = { user ->
                                // TODO: Handle navigation after register success
                            }
                        )
                    }
                }
            }
        }
    }
}