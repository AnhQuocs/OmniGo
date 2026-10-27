package com.example.omnigo.features.customer.ride.presentation.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

    @Inject
    lateinit var cancelRideBookingUseCase: com.example.omnigo.features.customer.ride.domain.usecase.CancelRideBookingUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        val bookingId = intent.getLongExtra(EXTRA_BOOKING_ID, -1L)
        if (bookingId > 0L && (rideTrackingSessionManager.activeRideState.value == null || rideTrackingSessionManager.activeRideState.value?.bookingId != bookingId)) {
            lifecycleScope.launch {
                rideTrackingSessionManager.restoreSessionIfAvailable()
            }
        }

        setOmniGoContent {
            val activeRideState = rideTrackingSessionManager.activeRideState.collectAsStateWithLifecycle().value
            var isCancelling by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

            TripMapScreen(
                activeRideState = activeRideState,
                onBackClick = { finish() },
                isCancelling = isCancelling,
                onCancelTrip = {
                    val currentId = activeRideState?.bookingId ?: bookingId
                    if (currentId > 0L) {
                        isCancelling = true
                        lifecycleScope.launch {
                            cancelRideBookingUseCase(currentId)
                            rideTrackingSessionManager.clearSession()
                            isCancelling = false
                            finish()
                        }
                    }
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val bookingId = intent.getLongExtra(EXTRA_BOOKING_ID, -1L)
        if (bookingId > 0L && (rideTrackingSessionManager.activeRideState.value == null || rideTrackingSessionManager.activeRideState.value?.bookingId != bookingId)) {
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
