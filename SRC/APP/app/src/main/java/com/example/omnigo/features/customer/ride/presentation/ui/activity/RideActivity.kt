package com.example.omnigo.features.customer.ride.presentation.ui.activity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.omnigo.BaseComponentActivity
import com.example.omnigo.features.customer.ride.presentation.ui.RideBookingScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RideActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        setOmniGoContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "ride_booking"
            ) {
                composable("ride_booking") {
                    RideBookingScreen(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}
