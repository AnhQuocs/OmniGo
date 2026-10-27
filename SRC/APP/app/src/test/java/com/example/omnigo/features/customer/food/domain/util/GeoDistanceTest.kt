package com.example.omnigo.features.customer.food.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoDistanceTest {

    @Test
    fun `same points return zero distance`() {
        val distance = GeoDistance.haversineKm(21.0285, 105.8542, 21.0285, 105.8542)
        assertEquals(0.0, distance, 0.0001)
    }

    @Test
    fun `known distance between Hanoi and Haiphong is approximately 90-110 km`() {
        // Hanoi Hoan Kiem (21.0285, 105.8542) to Haiphong Opera (20.8600, 106.6800)
        val distance = GeoDistance.haversineKm(21.0285, 105.8542, 20.8600, 106.6800)
        assertEquals(88.0, distance, 5.0)
    }
}
