package com.example.omnigo.features.main.presentation.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.omnigo.features.customer.activity.presentation.ui.ActivityScreen
import com.example.omnigo.features.customer.home.presentation.ui.HomeScreen
import com.example.omnigo.features.main.presentation.navigation.BottomNavDestination
import com.example.omnigo.features.main.presentation.ui.components.BottomNavVisibilityState
import com.example.omnigo.features.main.presentation.ui.components.LocalBottomNavVisibility
import com.example.omnigo.features.main.presentation.ui.components.OmniBottomNavBar
import com.example.omnigo.features.customer.profile.presentation.ui.AccountScreen
import com.example.omnigo.features.customer.promos.presentation.ui.PromosScreen
import com.example.omnigo.ui.theme.BackgroundLight

@Composable
fun MainCustomerScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val visibilityState = remember { BottomNavVisibilityState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavDestination.Home.route

    CompositionLocalProvider(LocalBottomNavVisibility provides visibilityState) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavDestination.Home.route,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                composable(BottomNavDestination.Home.route) {
                    HomeScreen(onNavigateToService = { serviceType ->
                        // Future: Navigate to OmniRide / OmniFood detailed flow
                    }, onNavigateToSearch = {
                        // Future: Navigate to Search Screen
                    }, onNavigateToNotifications = {
                        // Future: Navigate to Notifications Screen
                    })
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

            val bottomNavOffsetY = animateDpAsState(
                targetValue = if (visibilityState.visible) 0.dp else 120.dp,
                animationSpec = tween(durationMillis = 300),
                label = "bottomNavOffset",
            )

            OmniBottomNavBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, bottomNavOffsetY.value.roundToPx()) },
                currentRoute = currentRoute,
                onNavigateToDestination = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}