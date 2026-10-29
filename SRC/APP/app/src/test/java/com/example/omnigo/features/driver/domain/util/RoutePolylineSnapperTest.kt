package com.example.omnigo.features.driver.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutePolylineSnapperTest {

    @Test
    fun `snapAndTrim with empty route returns offRoute and empty remainingPolyline`() {
        val result = RoutePolylineSnapper.snapAndTrim(
            driverLat = 10.762622,
            driverLng = 106.660172,
            gpsBearing = 45f,
            routePoints = emptyList()
        )

        assertTrue(result.isOffRoute)
        assertTrue(result.remainingPolyline.isEmpty())
        assertEquals(0.0, result.remainingDistanceMeters, 0.01)
        assertEquals(45f, result.activeBearing, 0.1f)
        assertEquals(TurnManeuver.STRAIGHT, result.nextTurnManeuver)
    }

    @Test
    fun `snapAndTrim with single point within 35m returns ARRIVE maneuver`() {
        // ~15m north of driver
        val targetLat = 10.762622 + (15.0 / 6371000.0) * (180.0 / Math.PI)
        val targetLng = 106.660172

        val result = RoutePolylineSnapper.snapAndTrim(
            driverLat = 10.762622,
            driverLng = 106.660172,
            gpsBearing = 90f,
            routePoints = listOf(Pair(targetLat, targetLng))
        )

        assertFalse(result.isOffRoute)
        assertEquals(2, result.remainingPolyline.size)
        assertEquals(TurnManeuver.ARRIVE, result.nextTurnManeuver)
        assertEquals(15.0, result.remainingDistanceMeters, 1.5)
    }

    @Test
    fun `snapAndTrim projects driver onto middle segment and trims traversed vertices`() {
        // 3 segments heading North along 106.660000:
        // P0(10.7600, 106.6600) -> P1(10.7610, 106.6600) -> P2(10.7620, 106.6600) -> P3(10.7630, 106.6600)
        val route = listOf(
            Pair(10.7600, 106.6600),
            Pair(10.7610, 106.6600),
            Pair(10.7620, 106.6600),
            Pair(10.7630, 106.6600)
        )

        // Driver is halfway along segment 1 (between P1 and P2), slightly offset East by ~5m
        val driverLat = 10.7615
        val driverLng = 106.66004

        val result = RoutePolylineSnapper.snapAndTrim(
            driverLat = driverLat,
            driverLng = driverLng,
            gpsBearing = 180f,
            routePoints = route,
            lastSegmentIndex = 0
        )

        // Matched segment should be 1 (P1 -> P2)
        assertEquals(1, result.matchedSegmentIndex)
        assertFalse(result.isOffRoute)
        assertTrue(result.crossTrackMeters < 10.0)

        // Traversed vertices P0 and P1 must be removed; remainingPolyline is [S_1, P2, P3]
        assertEquals(3, result.remainingPolyline.size)
        assertEquals(10.7615, result.remainingPolyline[0].first, 0.0001)
        assertEquals(106.6600, result.remainingPolyline[0].second, 0.0001)
        assertEquals(route[2], result.remainingPolyline[1])
        assertEquals(route[3], result.remainingPolyline[2])

        // Heading North -> bearing ~ 0 degrees
        assertTrue(result.activeBearing < 5f || result.activeBearing > 355f)
    }

    @Test
    fun `snapAndTrim marks isOffRoute true and uses gpsBearing when crossTrack exceeds 50m`() {
        val route = listOf(
            Pair(10.7600, 106.6600),
            Pair(10.7620, 106.6600)
        )

        // Driver is ~110m East of the route
        val result = RoutePolylineSnapper.snapAndTrim(
            driverLat = 10.7610,
            driverLng = 106.6610,
            gpsBearing = 123f,
            routePoints = route
        )

        assertTrue(result.crossTrackMeters > 50.0)
        assertTrue(result.isOffRoute)
        assertEquals(123f, result.activeBearing, 0.5f)
    }

    @Test
    fun `snapAndTrim detects TURN_RIGHT and TURN_LEFT maneuvers accurately`() {
        // Segment 0 heads North: (10.7600, 106.6600) -> (10.7610, 106.6600) (~111m)
        // Segment 1 turns East (Right): (10.7610, 106.6600) -> (10.7610, 106.6610) (~109m)
        val rightTurnRoute = listOf(
            Pair(10.7600, 106.6600),
            Pair(10.7610, 106.6600),
            Pair(10.7610, 106.6610)
        )

        val rightSnap = RoutePolylineSnapper.snapAndTrim(
            driverLat = 10.7605,
            driverLng = 106.6600,
            gpsBearing = 0f,
            routePoints = rightTurnRoute
        )
        assertEquals(TurnManeuver.TURN_RIGHT, rightSnap.nextTurnManeuver)
        assertEquals(55.6, rightSnap.distanceToNextTurnMeters, 3.0)

        // Segment 1 turns West (Left): (10.7610, 106.6600) -> (10.7610, 106.6590)
        val leftTurnRoute = listOf(
            Pair(10.7600, 106.6600),
            Pair(10.7610, 106.6600),
            Pair(10.7610, 106.6590)
        )

        val leftSnap = RoutePolylineSnapper.snapAndTrim(
            driverLat = 10.7605,
            driverLng = 106.6600,
            gpsBearing = 0f,
            routePoints = leftTurnRoute
        )
        assertEquals(TurnManeuver.TURN_LEFT, leftSnap.nextTurnManeuver)
    }

    @Test
    fun `computeLookAheadTarget shifts coordinate ahead along bearing by offsetMeters`() {
        val startLat = 10.762622
        val startLng = 106.660172
        val target = RoutePolylineSnapper.computeLookAheadTarget(
            lat = startLat,
            lng = startLng,
            bearingDegrees = 0f, // Due North
            offsetMeters = 35.0
        )

        assertTrue(target.first > startLat)
        assertEquals(startLng, target.second, 0.000001)
        val dist = RideGeofenceCalculator.calculateDistanceMeters(
            startLat,
            startLng,
            target.first,
            target.second
        )
        assertEquals(35.0, dist, 0.5)
    }
}
