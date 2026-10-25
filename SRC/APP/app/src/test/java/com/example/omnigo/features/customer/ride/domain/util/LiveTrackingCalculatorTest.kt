package com.example.omnigo.features.customer.ride.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LiveTrackingCalculatorTest {

    @Test
    fun `calculateRemainingDistanceKm returns accurate distance between two points`() {
        // Ben Thanh Market (10.7721, 106.6983) to Bitexco Tower (10.7716, 106.7044) is ~0.67 km
        val distanceKm = LiveTrackingCalculator.calculateRemainingDistanceKm(
            fromLat = 10.7721,
            fromLng = 106.6983,
            toLat = 10.7716,
            toLng = 106.7044
        )
        assertEquals(0.67, distanceKm, 0.1)
    }

    @Test
    fun `calculateEtaMinutes returns 0 when within 50 meters`() {
        val eta = LiveTrackingCalculator.calculateEtaMinutes(
            distanceKm = 0.03, // 30 meters
            speedKmh = 20.0f
        )
        assertEquals(0, eta)
    }

    @Test
    fun `calculateEtaMinutes uses driver speed when speed is sufficient`() {
        // Distance: 5 km, Speed: 30 km/h -> 5 / 30 * 60 = 10 minutes
        val eta = LiveTrackingCalculator.calculateEtaMinutes(
            distanceKm = 5.0,
            speedKmh = 30.0f
        )
        assertEquals(10, eta)
    }

    @Test
    fun `calculateEtaMinutes uses fallback speed when speed is null or near zero`() {
        // Distance: 5 km, Fallback speed: 25 km/h -> 5 / 25 * 60 = 12 minutes
        val etaStationary = LiveTrackingCalculator.calculateEtaMinutes(
            distanceKm = 5.0,
            speedKmh = 1.0f
        )
        val etaNullSpeed = LiveTrackingCalculator.calculateEtaMinutes(
            distanceKm = 5.0,
            speedKmh = null
        )
        assertEquals(12, etaStationary)
        assertEquals(12, etaNullSpeed)
    }

    @Test
    fun `calculateEtaMinutes returns minimum 1 minute for small non-zero distance`() {
        val eta = LiveTrackingCalculator.calculateEtaMinutes(
            distanceKm = 0.1, // 100 meters
            speedKmh = 60.0f
        )
        assertEquals(1, eta)
    }
}
