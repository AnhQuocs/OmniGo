package com.example.omnigo.features.customer.ride.presentation.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.omnigo.BaseComponentActivity
import com.example.omnigo.features.customer.ride.data.local.RideTrackingSessionManager
import com.example.omnigo.features.customer.ride.presentation.ui.TripMapScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TripMapActivity : BaseComponentActivity() {

    @Inject
    lateinit var rideTrackingSessionManager: RideTrackingSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        val bookingId = intent.getLongExtra(EXTRA_BOOKING_ID, -1L)
        if (bookingId > 0L && rideTrackingSessionManager.activeRideState.value == null) {
            lifecycleScope.launch {
                rideTrackingSessionManager.restoreSessionIfAvailable()
            }
        }

        setOmniGoContent {
            val activeRideState = rideTrackingSessionManager.activeRideState.collectAsStateWithLifecycle().value

            TripMapScreen(
                activeRideState = activeRideState,
                onBackClick = { finish() }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val bookingId = intent.getLongExtra(EXTRA_BOOKING_ID, -1L)
        if (bookingId > 0L && rideTrackingSessionManager.activeRideState.value == null) {
            lifecycleScope.launch {
                rideTrackingSessionManager.restoreSessionIfAvailable()
            }
        }
    }

    companion object {
        const val EXTRA_BOOKING_ID = "EXTRA_BOOKING_ID"

        fun createIntent(context: Context, bookingId: Long): Intent {
            return Intent(context, TripMapActivity::class.java).apply {
                putExtra(EXTRA_BOOKING_ID, bookingId)
            }
        }
    }
}
