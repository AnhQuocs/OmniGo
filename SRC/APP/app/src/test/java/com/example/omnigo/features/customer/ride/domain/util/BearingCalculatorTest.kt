package com.example.omnigo.features.customer.ride.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class BearingCalculatorTest {

    @Test
    fun `calculateBearing directly North returns 0 degrees`() {
        val bearing = BearingCalculator.calculateBearing(
            lat1 = 10.0,
            lon1 = 106.0,
            lat2 = 11.0,
            lon2 = 106.0
        )
        assertEquals(0f, bearing, 0.5f)
    }

    @Test
    fun `calculateBearing directly East returns 90 degrees`() {
        val bearing = BearingCalculator.calculateBearing(
            lat1 = 0.0,
            lon1 = 0.0,
            lat2 = 0.0,
            lon2 = 1.0
        )
        assertEquals(90f, bearing, 0.5f)
    }

    @Test
    fun `calculateBearing directly South returns 180 degrees`() {
        val bearing = BearingCalculator.calculateBearing(
            lat1 = 11.0,
            lon1 = 106.0,
            lat2 = 10.0,
            lon2 = 106.0
        )
        assertEquals(180f, bearing, 0.5f)
    }

    @Test
    fun `calculateBearing directly West returns 270 degrees`() {
        val bearing = BearingCalculator.calculateBearing(
            lat1 = 0.0,
            lon1 = 1.0,
            lat2 = 0.0,
            lon2 = 0.0
        )
        assertEquals(270f, bearing, 0.5f)
    }

    @Test
    fun `normalizeBearing correctly normalizes negative and overflowing angles`() {
        assertEquals(315f, BearingCalculator.normalizeBearing(-45f), 0.01f)
        assertEquals(10f, BearingCalculator.normalizeBearing(730f), 0.01f)
        assertEquals(0f, BearingCalculator.normalizeBearing(360f), 0.01f)
        assertEquals(180f, BearingCalculator.normalizeBearing(180f), 0.01f)
    }
}
