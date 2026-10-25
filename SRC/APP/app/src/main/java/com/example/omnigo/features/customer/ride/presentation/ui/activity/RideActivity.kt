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

import androidx.lifecycle.lifecycleScope
import com.example.omnigo.features.customer.ride.data.local.RideSessionManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RideActivity : BaseComponentActivity() {

    @Inject
    lateinit var rideSessionManager: RideSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val extraBookingId = intent.getLongExtra(EXTRA_BOOKING_ID, -1L)
        if (extraBookingId > 0L) {
            lifecycleScope.launch {
                rideSessionManager.saveActiveBookingId(extraBookingId)
            }
        }

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

    companion object {
        const val EXTRA_BOOKING_ID = "EXTRA_BOOKING_ID"
    }
}
