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

import androidx.compose.foundation.layout.Column
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.features.customer.home.presentation.ui.components.ActiveTripHomeBanner
import com.example.omnigo.features.customer.home.presentation.ui.components.ActiveTripModalBottomSheet
import com.example.omnigo.features.customer.home.presentation.viewmodel.HomeViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainCustomerScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val visibilityState = remember { BottomNavVisibilityState() }
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

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
                    val context = androidx.compose.ui.platform.LocalContext.current
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToService = { serviceType ->
                            if (serviceType == "food") {
                                val intent = android.content.Intent(context, com.example.omnigo.features.customer.food.presentation.ui.activity.FoodActivity::class.java)
                                context.startActivity(intent)
                            } else if (serviceType == "ride") {
                                val intent = android.content.Intent(context, com.example.omnigo.features.customer.ride.presentation.ui.activity.RideActivity::class.java)
                                context.startActivity(intent)
                            }
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

            val bottomNavOffsetY = animateDpAsState(
                targetValue = if (visibilityState.visible) 0.dp else 120.dp,
                animationSpec = tween(durationMillis = 300),
                label = "bottomNavOffset",
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, bottomNavOffsetY.value.roundToPx()) }
            ) {
                ActiveTripHomeBanner(
                    activeRideState = homeUiState.activeRideState,
                    onClick = homeViewModel::onActiveTripBannerClick
                )

                OmniBottomNavBar(
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

            if (homeUiState.showActiveTripSheet && homeUiState.activeRideState != null) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val activeRide = homeUiState.activeRideState!!
                ActiveTripModalBottomSheet(
                    activeRideState = activeRide,
                    onDismissRequest = homeViewModel::dismissActiveTripSheet,
                    onOpenMapClick = {
                        val intent = com.example.omnigo.features.customer.ride.presentation.ui.activity.TripMapActivity.createIntent(
                            context = context,
                            bookingId = activeRide.bookingId
                        )
                        context.startActivity(intent)
                    },
                    onCancelTripClick = homeViewModel::promptCancelActiveTrip
                )
            }

            if (homeUiState.showCancelTripConfirmDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = homeViewModel::dismissCancelTripDialog,
                    title = {
                        androidx.compose.material3.Text(
                            text = androidx.compose.ui.res.stringResource(id = com.example.omnigo.R.string.ride_cancel_active_trip_dialog_title),
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            color = com.example.omnigo.ui.theme.TextPrimary
                        )
                    },
                    text = {
                        androidx.compose.material3.Text(
                            text = androidx.compose.ui.res.stringResource(id = com.example.omnigo.R.string.ride_cancel_active_trip_dialog_message),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = com.example.omnigo.ui.theme.TextSecondary
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = homeViewModel::confirmCancelActiveTrip,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = com.example.omnigo.ui.theme.ErrorColor
                            )
                        ) {
                            androidx.compose.material3.Text(
                                text = androidx.compose.ui.res.stringResource(id = com.example.omnigo.R.string.driver_trip_cancel_confirm)
                            )
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = homeViewModel::dismissCancelTripDialog
                        ) {
                            androidx.compose.material3.Text(
                                text = androidx.compose.ui.res.stringResource(id = com.example.omnigo.R.string.driver_trip_cancel_dismiss),
                                color = com.example.omnigo.ui.theme.TextSecondary
                            )
                        }
                    }
                )
            }
        }
    }
}