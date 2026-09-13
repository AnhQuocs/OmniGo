package com.example.omnigo.features.main.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.omnigo.features.driver.earnings.presentation.ui.DriverEarningsScreen
import com.example.omnigo.features.driver.home.presentation.ui.DriverHomeScreen
import com.example.omnigo.features.driver.profile.presentation.ui.DriverAccountScreen
import com.example.omnigo.features.driver.trips.presentation.ui.DriverTripsScreen
import com.example.omnigo.features.main.presentation.navigation.DriverBottomNavDestination
import com.example.omnigo.features.main.presentation.ui.components.DriverBottomNavBar
import com.example.omnigo.ui.theme.BackgroundLight

@Composable
fun MainDriverScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DriverBottomNavDestination.Home.route

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        NavHost(
            navController = navController,
            startDestination = DriverBottomNavDestination.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(DriverBottomNavDestination.Home.route) {
                DriverHomeScreen()
            }

            composable(DriverBottomNavDestination.Trips.route) {
                DriverTripsScreen()
            }

            composable(DriverBottomNavDestination.Earnings.route) {
                DriverEarningsScreen()
            }

            composable(DriverBottomNavDestination.Account.route) {
                DriverAccountScreen(
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }

        DriverBottomNavBar(
            currentRoute = currentRoute,
            onNavigateToDestination = { destination ->
                navController.navigate(destination.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
