package com.example.omnigo.features.driver.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RideGeofenceCalculatorTest {

    @Test
    fun calculateDistanceMeters_samePoint_returnsZero() {
        val distance = RideGeofenceCalculator.calculateDistanceMeters(
            lat1 = 10.762622,
            lon1 = 106.660172,
            lat2 = 10.762622,
            lon2 = 106.660172
        )
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun isWithinRadius_nullCoordinates_returnsFalse() {
        assertFalse(RideGeofenceCalculator.isWithinRadius(null, 106.0, 10.0, 106.0))
        assertFalse(RideGeofenceCalculator.isWithinRadius(10.0, null, 10.0, 106.0))
        assertFalse(RideGeofenceCalculator.isWithinRadius(10.0, 106.0, null, 106.0))
        assertFalse(RideGeofenceCalculator.isWithinRadius(10.0, 106.0, 10.0, null))
    }

    @Test
    fun isWithinRadius_boundary49Meters_returnsTrue() {
        val lat1 = 10.762622
        val lon1 = 106.660172
        // A point ~49 meters north
        val lat2 = lat1 + (49.0 / 6371000.0) * (180.0 / Math.PI)
        val lon2 = lon1

        val distance = RideGeofenceCalculator.calculateDistanceMeters(lat1, lon1, lat2, lon2)
        assertEquals(49.0, distance, 0.1)
        assertTrue(RideGeofenceCalculator.isWithinRadius(lat1, lon1, lat2, lon2, radiusMeters = 50.0))
    }

    @Test
    fun isWithinRadius_boundary50MetersExact_returnsTrue() {
        val lat1 = 10.762622
        val lon1 = 106.660172
        // A point exact 50.0 meters north
        val lat2 = lat1 + (50.0 / 6371000.0) * (180.0 / Math.PI)
        val lon2 = lon1

        val distance = RideGeofenceCalculator.calculateDistanceMeters(lat1, lon1, lat2, lon2)
        assertEquals(50.0, distance, 0.1)
        assertTrue(RideGeofenceCalculator.isWithinRadius(lat1, lon1, lat2, lon2, radiusMeters = 50.0))
    }

    @Test
    fun isWithinRadius_boundary51Meters_returnsFalse() {
        val lat1 = 10.762622
        val lon1 = 106.660172
        // A point ~51 meters north
        val lat2 = lat1 + (51.0 / 6371000.0) * (180.0 / Math.PI)
        val lon2 = lon1

        val distance = RideGeofenceCalculator.calculateDistanceMeters(lat1, lon1, lat2, lon2)
        assertEquals(51.0, distance, 0.1)
        assertFalse(RideGeofenceCalculator.isWithinRadius(lat1, lon1, lat2, lon2, radiusMeters = 50.0))
    }
}
