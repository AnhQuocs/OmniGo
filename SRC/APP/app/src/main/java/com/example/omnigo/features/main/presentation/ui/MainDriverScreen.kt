package com.example.omnigo.features.main.presentation.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.omnigo.features.driver.earnings.presentation.ui.DriverEarningsScreen
import com.example.omnigo.features.driver.food.presentation.ui.DriverActiveDeliveryScreen
import com.example.omnigo.features.driver.home.presentation.ui.DriverHomeScreen
import com.example.omnigo.features.driver.profile.presentation.ui.DriverAccountScreen
import com.example.omnigo.features.driver.ride.presentation.ui.DriverActiveTripScreen
import com.example.omnigo.features.driver.trips.presentation.ui.DriverTripsScreen
import com.example.omnigo.features.main.presentation.navigation.DriverBottomNavDestination
import com.example.omnigo.features.main.presentation.ui.components.BottomNavVisibilityState
import com.example.omnigo.features.main.presentation.ui.components.DriverBottomNavBar
import com.example.omnigo.features.main.presentation.ui.components.LocalBottomNavVisibility
import com.example.omnigo.features.main.presentation.viewmodel.DriverMainViewModel
import com.example.omnigo.ui.theme.BackgroundLight

@Composable
fun MainDriverScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverMainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val visibilityState = remember { BottomNavVisibilityState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DriverBottomNavDestination.Home.route
    val isTripOrDeliveryActive = currentRoute.startsWith("driver_active_delivery") || currentRoute.startsWith("driver_active_trip")
    val hasActiveTask by viewModel.hasActiveTask.collectAsStateWithLifecycle()

    // Always keep BottomNavBar visible on Home (interactive map screen)
    val isHomeScreen = currentRoute == DriverBottomNavDestination.Home.route
    LaunchedEffect(currentRoute) {
        if (isHomeScreen) {
            visibilityState.visible = true
        }
    }

    val isBottomNavVisible = if (isHomeScreen) true else visibilityState.visible
    val bottomNavOffsetY by animateDpAsState(
        targetValue = if (isBottomNavVisible) 0.dp else 120.dp,
        animationSpec = tween(durationMillis = 300),
        label = "driverBottomNavOffset"
    )

    CompositionLocalProvider(LocalBottomNavVisibility provides visibilityState) {
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
                DriverHomeScreen(
                    onNavigateToActiveDelivery = { orderId, restaurantLat, restaurantLng ->
                        val route = buildString {
                            append("driver_active_delivery/$orderId")
                            if (restaurantLat != null && restaurantLng != null) {
                                append("?restaurantLat=$restaurantLat&restaurantLng=$restaurantLng")
                            }
                        }
                        navController.navigate(route)
                    },
                    onNavigateToActiveTrip = { bookingId ->
                        navController.navigate("driver_active_trip/$bookingId")
                    }
                )
            }

            composable(
                route = "driver_active_delivery/{orderId}?restaurantLat={restaurantLat}&restaurantLng={restaurantLng}",
                arguments = listOf(
                    navArgument("orderId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("restaurantLat") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("restaurantLng") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
                DriverActiveDeliveryScreen(
                    orderId = orderId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "driver_active_trip/{bookingId}?pickupLat={pickupLat}&pickupLng={pickupLng}&dropoffLat={dropoffLat}&dropoffLng={dropoffLng}&price={price}&paymentMethod={paymentMethod}",
                arguments = listOf(
                    navArgument("bookingId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("pickupLat") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("pickupLng") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("dropoffLat") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("dropoffLng") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("price") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("paymentMethod") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                DriverActiveTripScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(DriverBottomNavDestination.Trips.route) {
                DriverTripsScreen(
                    onResumeActiveTrip = { booking ->
                        navController.navigate(
                            "driver_active_trip/${booking.bookingId}?pickupLat=${booking.startLatitude}&pickupLng=${booking.startLongitude}&dropoffLat=${booking.endLatitude}&dropoffLng=${booking.endLongitude}&price=${booking.price}&paymentMethod=${booking.paymentMethod ?: "CASH"}"
                        )
                    },
                    onResumeActiveFoodOrder = { order ->
                        val route = buildString {
                            append("driver_active_delivery/${order.orderId}")
                            if (order.restaurantLatitude != null && order.restaurantLongitude != null) {
                                append("?restaurantLat=${order.restaurantLatitude}&restaurantLng=${order.restaurantLongitude}")
                            }
                        }
                        navController.navigate(route)
                    }
                )
            }

            composable(DriverBottomNavDestination.Earnings.route) {
                DriverEarningsScreen(
                    onNavigateToTrips = {
                        navController.navigate(DriverBottomNavDestination.Trips.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(DriverBottomNavDestination.Account.route) {
                DriverAccountScreen(
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }

        if (!isTripOrDeliveryActive) {
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
                hasActiveTask = hasActiveTask,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, bottomNavOffsetY.roundToPx()) }
            )
        }
    }
}
}
