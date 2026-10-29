package com.example.omnigo.features.driver.domain.util

import com.example.omnigo.features.customer.ride.domain.util.BearingCalculator
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class TurnManeuver {
    STRAIGHT,
    TURN_LEFT,
    TURN_RIGHT,
    U_TURN,
    ARRIVE
}

data class RouteSnapResult(
    val snappedLat: Double,
    val snappedLng: Double,
    val activeBearing: Float,
    val matchedSegmentIndex: Int,
    val crossTrackMeters: Double,
    val isOffRoute: Boolean,
    val remainingPolyline: List<Pair<Double, Double>>,
    val remainingDistanceMeters: Double,
    val nextTurnManeuver: TurnManeuver,
    val distanceToNextTurnMeters: Double
)

object RoutePolylineSnapper {

    private const val EARTH_RADIUS_METERS = 6371000.0
    const val FORWARD_WINDOW_SIZE = 12
    const val WINDOW_ACCEPT_THRESHOLD_METERS = 40.0
    const val SNAP_BEARING_THRESHOLD_METERS = 35.0
    const val OFF_ROUTE_THRESHOLD_METERS = 50.0
    const val ARRIVE_DISTANCE_THRESHOLD_METERS = 35.0
    const val MIN_TURN_SEGMENT_LENGTH_METERS = 8.0
    const val DEFAULT_LOOK_AHEAD_METERS = 35.0

    /**
     * Projects the driver's real-time coordinate onto the route polyline, trims away
     * traversed vertices behind the vehicle, computes remaining distance, active heading,
     * next turn maneuver, and off-route status.
     */
    fun snapAndTrim(
        driverLat: Double,
        driverLng: Double,
        gpsBearing: Float?,
        routePoints: List<Pair<Double, Double>>,
        lastSegmentIndex: Int = 0
    ): RouteSnapResult {
        val fallbackBearing = BearingCalculator.normalizeBearing(gpsBearing ?: 0f)

        if (routePoints.isEmpty()) {
            return RouteSnapResult(
                snappedLat = driverLat,
                snappedLng = driverLng,
                activeBearing = fallbackBearing,
                matchedSegmentIndex = 0,
                crossTrackMeters = 0.0,
                isOffRoute = true,
                remainingPolyline = emptyList(),
                remainingDistanceMeters = 0.0,
                nextTurnManeuver = TurnManeuver.STRAIGHT,
                distanceToNextTurnMeters = 0.0
            )
        }

        if (routePoints.size == 1) {
            val target = routePoints[0]
            val dist = RideGeofenceCalculator.calculateDistanceMeters(
                driverLat,
                driverLng,
                target.first,
                target.second
            )
            val bearing = if (dist > 1.0) {
                BearingCalculator.calculateBearing(driverLat, driverLng, target.first, target.second)
            } else {
                fallbackBearing
            }
            return RouteSnapResult(
                snappedLat = driverLat,
                snappedLng = driverLng,
                activeBearing = bearing,
                matchedSegmentIndex = 0,
                crossTrackMeters = 0.0,
                isOffRoute = dist > OFF_ROUTE_THRESHOLD_METERS,
                remainingPolyline = listOf(Pair(driverLat, driverLng), target),
                remainingDistanceMeters = dist,
                nextTurnManeuver = if (dist < ARRIVE_DISTANCE_THRESHOLD_METERS) {
                    TurnManeuver.ARRIVE
                } else {
                    TurnManeuver.STRAIGHT
                },
                distanceToNextTurnMeters = dist
            )
        }

        val lastValidSegment = routePoints.size - 2
        val safeStartIndex = lastSegmentIndex.coerceIn(0, lastValidSegment)
        val windowEndIndex = (safeStartIndex + FORWARD_WINDOW_SIZE).coerceAtMost(lastValidSegment)

        var bestMatch = findBestSegmentInRange(
            driverLat = driverLat,
            driverLng = driverLng,
            routePoints = routePoints,
            startIdx = safeStartIndex,
            endIdx = windowEndIndex
        )

        if (bestMatch.crossTrackMeters > WINDOW_ACCEPT_THRESHOLD_METERS) {
            bestMatch = findBestSegmentInRange(
                driverLat = driverLat,
                driverLng = driverLng,
                routePoints = routePoints,
                startIdx = 0,
                endIdx = lastValidSegment
            )
        }

        val k = bestMatch.segmentIndex
        val snappedPoint = Pair(bestMatch.snappedLat, bestMatch.snappedLng)
        val remainingPolyline = listOf(snappedPoint) + routePoints.drop(k + 1)
        val remainingDistanceMeters = calculatePolylineLengthMeters(remainingPolyline)

        val segmentBearing = resolveSegmentBearing(routePoints, k, fallbackBearing)
        val activeBearing = if (bestMatch.crossTrackMeters <= SNAP_BEARING_THRESHOLD_METERS) {
            segmentBearing
        } else {
            gpsBearing?.let { BearingCalculator.normalizeBearing(it) } ?: segmentBearing
        }

        val (maneuver, distToTurn) = detectNextTurnManeuver(
            snappedPoint = snappedPoint,
            routePoints = routePoints,
            matchedSegmentIndex = k,
            initialSegmentBearing = segmentBearing,
            remainingDistanceMeters = remainingDistanceMeters
        )

        return RouteSnapResult(
            snappedLat = bestMatch.snappedLat,
            snappedLng = bestMatch.snappedLng,
            activeBearing = activeBearing,
            matchedSegmentIndex = k,
            crossTrackMeters = bestMatch.crossTrackMeters,
            isOffRoute = bestMatch.crossTrackMeters > OFF_ROUTE_THRESHOLD_METERS,
            remainingPolyline = remainingPolyline,
            remainingDistanceMeters = remainingDistanceMeters,
            nextTurnManeuver = maneuver,
            distanceToNextTurnMeters = distToTurn
        )
    }

    /**
     * Offsets the camera target ahead of the vehicle nose along [bearingDegrees] by [offsetMeters]
     * so that the vehicle sits in the lower 1/3 of the 3D tilted navigation viewport.
     */
    fun computeLookAheadTarget(
        lat: Double,
        lng: Double,
        bearingDegrees: Float,
        offsetMeters: Double = DEFAULT_LOOK_AHEAD_METERS
    ): Pair<Double, Double> {
        val bearingRad = Math.toRadians(bearingDegrees.toDouble())
        val latRad = Math.toRadians(lat)
        val cosLat = cos(latRad).coerceAtLeast(1e-6)

        val deltaLat = (offsetMeters * cos(bearingRad) / EARTH_RADIUS_METERS) * (180.0 / Math.PI)
        val deltaLng = (offsetMeters * sin(bearingRad) / (EARTH_RADIUS_METERS * cosLat)) * (180.0 / Math.PI)

        return Pair(lat + deltaLat, lng + deltaLng)
    }

    private data class SegmentProjection(
        val segmentIndex: Int,
        val snappedLat: Double,
        val snappedLng: Double,
        val crossTrackMeters: Double
    )

    private fun findBestSegmentInRange(
        driverLat: Double,
        driverLng: Double,
        routePoints: List<Pair<Double, Double>>,
        startIdx: Int,
        endIdx: Int
    ): SegmentProjection {
        var best: SegmentProjection? = null
        val cosLat = cos(Math.toRadians(driverLat))

        for (i in startIdx..endIdx) {
            val p1 = routePoints[i]
            val p2 = routePoints[i + 1]
            val projection = projectPointOntoSegment(
                driverLat = driverLat,
                driverLng = driverLng,
                lat1 = p1.first,
                lng1 = p1.second,
                lat2 = p2.first,
                lng2 = p2.second,
                cosLat = cosLat,
                segmentIndex = i
            )
            if (best == null || projection.crossTrackMeters < best.crossTrackMeters) {
                best = projection
            }
        }

        return best ?: SegmentProjection(
            segmentIndex = startIdx,
            snappedLat = routePoints[startIdx].first,
            snappedLng = routePoints[startIdx].second,
            crossTrackMeters = 0.0
        )
    }

    private fun projectPointOntoSegment(
        driverLat: Double,
        driverLng: Double,
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double,
        cosLat: Double,
        segmentIndex: Int
    ): SegmentProjection {
        val dx = (lng2 - lng1) * cosLat
        val dy = lat2 - lat1
        val lenSq = dx * dx + dy * dy

        val t = if (lenSq <= 1e-12) {
            0.0
        } else {
            val px = (driverLng - lng1) * cosLat
            val py = driverLat - lat1
            ((px * dx + py * dy) / lenSq).coerceIn(0.0, 1.0)
        }

        val snappedLat = lat1 + t * (lat2 - lat1)
        val snappedLng = lng1 + t * (lng2 - lng1)
        val crossTrack = RideGeofenceCalculator.calculateDistanceMeters(
            driverLat,
            driverLng,
            snappedLat,
            snappedLng
        )

        return SegmentProjection(
            segmentIndex = segmentIndex,
            snappedLat = snappedLat,
            snappedLng = snappedLng,
            crossTrackMeters = crossTrack
        )
    }

    private fun resolveSegmentBearing(
        routePoints: List<Pair<Double, Double>>,
        segmentIndex: Int,
        fallbackBearing: Float
    ): Float {
        for (i in segmentIndex until routePoints.size - 1) {
            val a = routePoints[i]
            val b = routePoints[i + 1]
            val dist = RideGeofenceCalculator.calculateDistanceMeters(a.first, a.second, b.first, b.second)
            if (dist >= 0.5) {
                return BearingCalculator.calculateBearing(a.first, a.second, b.first, b.second)
            }
        }
        return fallbackBearing
    }

    private fun detectNextTurnManeuver(
        snappedPoint: Pair<Double, Double>,
        routePoints: List<Pair<Double, Double>>,
        matchedSegmentIndex: Int,
        initialSegmentBearing: Float,
        remainingDistanceMeters: Double
    ): Pair<TurnManeuver, Double> {
        if (remainingDistanceMeters < ARRIVE_DISTANCE_THRESHOLD_METERS) {
            return Pair(TurnManeuver.ARRIVE, remainingDistanceMeters)
        }

        val firstVertexAhead = routePoints[matchedSegmentIndex + 1]
        var accumulatedDistance = RideGeofenceCalculator.calculateDistanceMeters(
            snappedPoint.first,
            snappedPoint.second,
            firstVertexAhead.first,
            firstVertexAhead.second
        )
        var currentBearing = initialSegmentBearing

        for (m in (matchedSegmentIndex + 1) until (routePoints.size - 1)) {
            val pA = routePoints[m]
            val pB = routePoints[m + 1]
            val segLen = RideGeofenceCalculator.calculateDistanceMeters(
                pA.first,
                pA.second,
                pB.first,
                pB.second
            )
            if (segLen >= MIN_TURN_SEGMENT_LENGTH_METERS) {
                val nextBearing = BearingCalculator.calculateBearing(
                    pA.first,
                    pA.second,
                    pB.first,
                    pB.second
                )
                val delta = normalizeSignedAngleDelta(nextBearing - currentBearing)
                when {
                    abs(delta) >= 135f -> return Pair(TurnManeuver.U_TURN, accumulatedDistance)
                    delta >= 30f -> return Pair(TurnManeuver.TURN_RIGHT, accumulatedDistance)
                    delta <= -30f -> return Pair(TurnManeuver.TURN_LEFT, accumulatedDistance)
                    else -> currentBearing = nextBearing
                }
            }
            accumulatedDistance += segLen
        }

        return Pair(TurnManeuver.STRAIGHT, remainingDistanceMeters)
    }

    private fun normalizeSignedAngleDelta(deltaDegrees: Float): Float {
        var delta = deltaDegrees % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return delta
    }

    private fun calculatePolylineLengthMeters(points: List<Pair<Double, Double>>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += RideGeofenceCalculator.calculateDistanceMeters(
                points[i].first,
                points[i].second,
                points[i + 1].first,
                points[i + 1].second
            )
        }
        return total
    }
}
