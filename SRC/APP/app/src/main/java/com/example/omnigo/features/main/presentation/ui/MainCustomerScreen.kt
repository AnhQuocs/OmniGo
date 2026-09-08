package com.example.omnigo.features.main.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.omnigo.features.activity.presentation.ui.ActivityScreen
import com.example.omnigo.features.home.presentation.ui.HomeScreen
import com.example.omnigo.features.main.presentation.navigation.BottomNavDestination
import com.example.omnigo.features.main.presentation.ui.components.OmniBottomNavBar
import com.example.omnigo.features.profile.presentation.ui.AccountScreen
import com.example.omnigo.features.promos.presentation.ui.PromosScreen
import com.example.omnigo.ui.theme.BackgroundLight

@Composable
fun MainCustomerScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavDestination.Home.route

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        bottomBar = {
            OmniBottomNavBar(
                currentRoute = currentRoute,
                onNavigateToDestination = { destination ->
                    bottomNavController.navigate(destination.route) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavDestination.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(BottomNavDestination.Home.route) {
                HomeScreen(
                    onNavigateToService = { serviceType ->
                        // Future: Navigate to OmniRide / OmniFood detailed flow
                    },
                    onNavigateToSearch = {
                        // Future: Navigate to Search Screen
                    },
                    onNavigateToNotifications = {
                        // Future: Navigate to Notifications Screen
                    }
                )
            }

            composable(BottomNavDestination.Activity.route) {
                ActivityScreen()
            }

            composable(BottomNavDestination.Promos.route) {
                PromosScreen()
            }

            composable(BottomNavDestination.Account.route) {
                AccountScreen(
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }
}
