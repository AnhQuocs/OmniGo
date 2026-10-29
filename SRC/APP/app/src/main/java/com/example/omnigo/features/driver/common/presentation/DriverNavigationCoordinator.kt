package com.example.omnigo.features.driver.common.presentation

import com.example.omnigo.core.location.tracker.DriverNavigationLocationSource
import com.example.omnigo.core.location.tracker.OsrmRoutingService
import com.example.omnigo.features.driver.domain.model.DriverLocationUpdate
import com.example.omnigo.features.driver.domain.util.RoutePolylineSnapper
import com.example.omnigo.features.driver.domain.util.TurnManeuver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.abs

data class DriverNavigationUiState(
    val driverLat: Double? = null,
    val driverLng: Double? = null,
    val activeBearing: Float = 0f,
    val targetLat: Double? = null,
    val targetLng: Double? = null,
    val targetAddress: String = "",
    val secondaryLat: Double? = null,
    val secondaryLng: Double? = null,
    val remainingPolyline: List<Pair<Double, Double>> = emptyList(),
    val remainingDistanceMeters: Double = 0.0,
    val etaMinutes: Int = 0,
    val nextTurnManeuver: TurnManeuver = TurnManeuver.STRAIGHT,
    val distanceToNextTurnMeters: Double = 0.0,
    val vehicleType: String = "BIKE",
    val isRouteLoading: Boolean = false
)

class DriverNavigationCoordinator(
    private val osrmRoutingService: OsrmRoutingService,
    private val navigationLocationSource: DriverNavigationLocationSource,
    private val clock: () -> Long
) {

    @Inject
    constructor(
        osrmRoutingService: OsrmRoutingService,
        navigationLocationSource: DriverNavigationLocationSource
    ) : this(
        osrmRoutingService = osrmRoutingService,
        navigationLocationSource = navigationLocationSource,
        clock = { System.currentTimeMillis() }
    )

    companion object {
        const val REROUTE_COOLDOWN_MS = 8000L
        private const val CITY_SPEED_METERS_PER_MIN = 400.0
        private const val COORDINATE_EPSILON = 0.00001
    }

    private val _navigationState = MutableStateFlow(DriverNavigationUiState())
    val navigationState: StateFlow<DriverNavigationUiState> = _navigationState.asStateFlow()

    private var fullRoutePoints: List<Pair<Double, Double>> = emptyList()
    private var lastMatchedSegmentIndex: Int = 0
    private var lastRerouteTimestampMs: Long = 0L

    fun observeLocationUpdates(): Flow<DriverLocationUpdate> {
        return navigationLocationSource.observeLocationUpdates()
    }

    suspend fun updateDestination(
        targetLat: Double?,
        targetLng: Double?,
        targetAddress: String,
        vehicleType: String = "BIKE",
        secondaryLat: Double? = null,
        secondaryLng: Double? = null
    ) {
        val prev = _navigationState.value
        val targetChanged = !isSameCoordinate(prev.targetLat, targetLat) ||
                !isSameCoordinate(prev.targetLng, targetLng)

        _navigationState.update {
            it.copy(
                targetLat = targetLat,
                targetLng = targetLng,
                targetAddress = targetAddress,
                vehicleType = vehicleType,
                secondaryLat = secondaryLat,
                secondaryLng = secondaryLng
            )
        }

        if (targetLat == null || targetLng == null) {
            fullRoutePoints = emptyList()
            lastMatchedSegmentIndex = 0
            _navigationState.update {
                it.copy(
                    remainingPolyline = emptyList(),
                    remainingDistanceMeters = 0.0,
                    etaMinutes = 0,
                    nextTurnManeuver = TurnManeuver.STRAIGHT,
                    distanceToNextTurnMeters = 0.0,
                    isRouteLoading = false
                )
            }
            return
        }

        if (targetChanged || fullRoutePoints.isEmpty()) {
            lastMatchedSegmentIndex = 0
            lastRerouteTimestampMs = 0L
            fullRoutePoints = emptyList()

            val currentDriverLat = _navigationState.value.driverLat
            val currentDriverLng = _navigationState.value.driverLng
            if (currentDriverLat != null && currentDriverLng != null) {
                fetchAndSnapRoute(
                    driverLat = currentDriverLat,
                    driverLng = currentDriverLng,
                    targetLat = targetLat,
                    targetLng = targetLng,
                    vehicleType = vehicleType,
                    gpsBearing = _navigationState.value.activeBearing
                )
            }
        }
    }

    suspend fun onLocationTick(
        lat: Double,
        lng: Double,
        bearing: Float? = null
    ) {
        val current = _navigationState.value
        val targetLat = current.targetLat
        val targetLng = current.targetLng

        if (targetLat == null || targetLng == null) {
            _navigationState.update {
                it.copy(
                    driverLat = lat,
                    driverLng = lng,
                    activeBearing = bearing ?: it.activeBearing
                )
            }
            return
        }

        val now = clock()
        if (fullRoutePoints.isEmpty()) {
            _navigationState.update {
                it.copy(
                    driverLat = lat,
                    driverLng = lng,
                    activeBearing = bearing ?: it.activeBearing
                )
            }
            if (!current.isRouteLoading &&
                (lastRerouteTimestampMs == 0L || now - lastRerouteTimestampMs >= REROUTE_COOLDOWN_MS)
            ) {
                fetchAndSnapRoute(
                    driverLat = lat,
                    driverLng = lng,
                    targetLat = targetLat,
                    targetLng = targetLng,
                    vehicleType = current.vehicleType,
                    gpsBearing = bearing
                )
            }
            return
        }

        val snap = RoutePolylineSnapper.snapAndTrim(
            driverLat = lat,
            driverLng = lng,
            gpsBearing = bearing ?: current.activeBearing,
            routePoints = fullRoutePoints,
            lastSegmentIndex = lastMatchedSegmentIndex
        )
        lastMatchedSegmentIndex = snap.matchedSegmentIndex

        _navigationState.update {
            it.copy(
                driverLat = lat,
                driverLng = lng,
                activeBearing = snap.activeBearing,
                remainingPolyline = snap.remainingPolyline,
                remainingDistanceMeters = snap.remainingDistanceMeters,
                etaMinutes = computeEtaMinutes(snap.remainingDistanceMeters),
                nextTurnManeuver = snap.nextTurnManeuver,
                distanceToNextTurnMeters = snap.distanceToNextTurnMeters
            )
        }

        if (snap.isOffRoute &&
            !_navigationState.value.isRouteLoading &&
            (lastRerouteTimestampMs == 0L || now - lastRerouteTimestampMs >= REROUTE_COOLDOWN_MS)
        ) {
            fetchAndSnapRoute(
                driverLat = lat,
                driverLng = lng,
                targetLat = targetLat,
                targetLng = targetLng,
                vehicleType = current.vehicleType,
                gpsBearing = bearing
            )
        }
    }

    private suspend fun fetchAndSnapRoute(
        driverLat: Double,
        driverLng: Double,
        targetLat: Double,
        targetLng: Double,
        vehicleType: String,
        gpsBearing: Float?
    ) {
        lastRerouteTimestampMs = clock()
        _navigationState.update {
            it.copy(
                driverLat = driverLat,
                driverLng = driverLng,
                isRouteLoading = true
            )
        }

        val fetchedRoute = osrmRoutingService.getRouteCoordinates(
            startLat = driverLat,
            startLng = driverLng,
            endLat = targetLat,
            endLng = targetLng,
            vehicleType = vehicleType
        )

        val effectiveRoute = if (fetchedRoute.size >= 2) {
            fetchedRoute
        } else {
            listOf(Pair(driverLat, driverLng), Pair(targetLat, targetLng))
        }

        fullRoutePoints = effectiveRoute
        lastMatchedSegmentIndex = 0

        val snap = RoutePolylineSnapper.snapAndTrim(
            driverLat = driverLat,
            driverLng = driverLng,
            gpsBearing = gpsBearing ?: _navigationState.value.activeBearing,
            routePoints = fullRoutePoints,
            lastSegmentIndex = 0
        )
        lastMatchedSegmentIndex = snap.matchedSegmentIndex

        _navigationState.update {
            it.copy(
                driverLat = driverLat,
                driverLng = driverLng,
                activeBearing = snap.activeBearing,
                remainingPolyline = snap.remainingPolyline,
                remainingDistanceMeters = snap.remainingDistanceMeters,
                etaMinutes = computeEtaMinutes(snap.remainingDistanceMeters),
                nextTurnManeuver = snap.nextTurnManeuver,
                distanceToNextTurnMeters = snap.distanceToNextTurnMeters,
                isRouteLoading = false
            )
        }
    }

    private fun computeEtaMinutes(remainingDistanceMeters: Double): Int {
        if (remainingDistanceMeters <= RoutePolylineSnapper.ARRIVE_DISTANCE_THRESHOLD_METERS) {
            return 0
        }
        return ceil(remainingDistanceMeters / CITY_SPEED_METERS_PER_MIN).toInt().coerceAtLeast(1)
    }

    private fun isSameCoordinate(a: Double?, b: Double?): Boolean {
        if (a == null && b == null) return true
        if (a == null || b == null) return false
        return abs(a - b) <= COORDINATE_EPSILON
    }
}
