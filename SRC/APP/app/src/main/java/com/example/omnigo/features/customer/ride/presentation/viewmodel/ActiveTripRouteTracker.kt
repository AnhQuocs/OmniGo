package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.core.location.tracker.OsrmRoutingService
import com.example.omnigo.features.customer.ride.domain.util.LiveTrackingCalculator
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TripRoutePhase { TO_PICKUP, TO_DROPOFF }

/**
 * Keeps the polyline of the live trip up to date: driver -> pickup while the driver is on the way,
 * then driver -> dropoff once the ride is in progress. The route is only re-requested when the
 * phase changes or the driver has moved far enough from the origin of the last request, so we
 * don't hit the routing backend on every GPS tick.
 */
class ActiveTripRouteTracker @Inject constructor(
    private val osrmRoutingService: OsrmRoutingService
) {
    private var routeJob: Job? = null
    private var lastOriginLat: Double? = null
    private var lastOriginLng: Double? = null
    private var lastPhase: TripRoutePhase? = null

    fun onDriverLocation(
        scope: CoroutineScope,
        driverLat: Double,
        driverLng: Double,
        phase: TripRoutePhase,
        targetLat: Double,
        targetLng: Double,
        vehicleType: String,
        updateState: ((RideBookingUiState) -> RideBookingUiState) -> Unit
    ) {
        if (lastPhase != phase) {
            routeJob?.cancel()
            routeJob = null
        } else if (routeJob?.isActive == true) {
            return
        }
        if (!shouldRefetch(phase, driverLat, driverLng)) return

        lastOriginLat = driverLat
        lastOriginLng = driverLng
        lastPhase = phase
        routeJob = scope.launch {
            val coordinates = osrmRoutingService.getRouteCoordinates(
                driverLat, driverLng, targetLat, targetLng, vehicleType
            )
            if (coordinates.isNotEmpty() && lastPhase == phase) {
                updateState { it.copy(activeTripRouteCoordinates = coordinates) }
            }
        }
    }

    internal fun shouldRefetch(phase: TripRoutePhase, driverLat: Double, driverLng: Double): Boolean {
        val originLat = lastOriginLat
        val originLng = lastOriginLng
        if (originLat == null || originLng == null || lastPhase != phase) return true
        val movedKm = LiveTrackingCalculator.calculateRemainingDistanceKm(originLat, originLng, driverLat, driverLng)
        return movedKm >= REFETCH_DISTANCE_KM
    }

    fun reset() {
        routeJob?.cancel()
        routeJob = null
        lastOriginLat = null
        lastOriginLng = null
        lastPhase = null
    }

    companion object {
        const val REFETCH_DISTANCE_KM = 0.15
    }
}
