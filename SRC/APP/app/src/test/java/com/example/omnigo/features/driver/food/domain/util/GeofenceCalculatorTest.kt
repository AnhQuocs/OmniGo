package com.example.omnigo.features.driver.food.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeofenceCalculatorTest {

    // Landmark: Ben Thanh Market, District 1, HCMC: 10.7725, 106.6980
    private val benThanhLat = 10.7725
    private val benThanhLng = 106.6980

    @Test
    fun `calculateDistanceMeters returns 0 for identical coordinates`() {
        val distance = GeofenceCalculator.calculateDistanceMeters(
            benThanhLat, benThanhLng,
            benThanhLat, benThanhLng
        )
        assertEquals(0.0, distance, 0.01)
    }

    @Test
    fun `isWithinRadius returns true when distance is less than 50m`() {
        // ~25 meters offset (approx 0.00022 degrees latitude is ~24.5m)
        val nearbyLat = benThanhLat + 0.00022
        val nearbyLng = benThanhLng

        val distance = GeofenceCalculator.calculateDistanceMeters(
            benThanhLat, benThanhLng,
            nearbyLat, nearbyLng
        )
        assertTrue(distance < 50.0)

        val within = GeofenceCalculator.isWithinRadius(
            benThanhLat, benThanhLng,
            nearbyLat, nearbyLng,
            radiusMeters = 50.0
        )
        assertTrue(within)
    }

    @Test
    fun `isWithinRadius returns false when distance exceeds 50m`() {
        // ~110 meters offset (approx 0.001 degrees latitude is ~111m)
        val farLat = benThanhLat + 0.0010
        val farLng = benThanhLng

        val distance = GeofenceCalculator.calculateDistanceMeters(
            benThanhLat, benThanhLng,
            farLat, farLng
        )
        assertTrue(distance > 50.0)

        val within = GeofenceCalculator.isWithinRadius(
            benThanhLat, benThanhLng,
            farLat, farLng,
            radiusMeters = 50.0
        )
        assertFalse(within)
    }

    @Test
    fun `isWithinRadius returns false when any coordinate is null`() {
        assertFalse(GeofenceCalculator.isWithinRadius(null, benThanhLng, benThanhLat, benThanhLng))
        assertFalse(GeofenceCalculator.isWithinRadius(benThanhLat, null, benThanhLat, benThanhLng))
        assertFalse(GeofenceCalculator.isWithinRadius(benThanhLat, benThanhLng, null, benThanhLng))
        assertFalse(GeofenceCalculator.isWithinRadius(benThanhLat, benThanhLng, benThanhLat, null))
        assertFalse(GeofenceCalculator.isWithinRadius(null, null, null, null))
    }
}
