package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.PricingResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PricingMapperTest {

    private val gson = Gson()

    @Test
    fun `maps backend pricing response json correctly to domain model`() {
        val json = """
            {
                "baseFare": 15000.0,
                "distanceFare": 25000.0,
                "durationFare": 5000.0,
                "surgeMultiplier": 1.2,
                "surgeLevel": "MEDIUM",
                "totalPrice": 54000.0,
                "distanceInKm": 3.8,
                "estimatedDurationMinutes": 12,
                "h3Index": "8865660829fffff"
            }
        """.trimIndent()

        val response = gson.fromJson(json, PricingResponse::class.java)
        val domain = response.toDomain()

        assertEquals(15000.0, domain.baseFare, 0.001)
        assertEquals(25000.0, domain.distanceFare, 0.001)
        assertEquals(5000.0, domain.durationFare, 0.001)
        assertEquals(1.2, domain.surgeMultiplier, 0.001)
        assertEquals("MEDIUM", domain.surgeLevel)
        assertEquals(54000.0, domain.totalPrice, 0.001)
        assertEquals(3.8, domain.distanceInKm, 0.001)
        assertEquals(12, domain.estimatedDurationMinutes)
        assertEquals("8865660829fffff", domain.h3Index)
    }

    @Test
    fun `handles null fields gracefully with default values`() {
        val response = PricingResponse(
            baseFare = null,
            distanceFare = null,
            durationFare = null,
            surgeMultiplier = null,
            surgeLevel = null,
            totalPrice = null,
            distanceInKm = null,
            estimatedDurationMinutes = null,
            h3Index = null
        )

        val domain = response.toDomain()

        assertEquals(0.0, domain.baseFare, 0.001)
        assertEquals(0.0, domain.distanceFare, 0.001)
        assertEquals(0.0, domain.durationFare, 0.001)
        assertEquals(1.0, domain.surgeMultiplier, 0.001)
        assertNull(domain.surgeLevel)
        assertEquals(0.0, domain.totalPrice, 0.001)
        assertEquals(0.0, domain.distanceInKm, 0.001)
        assertEquals(0, domain.estimatedDurationMinutes)
        assertNull(domain.h3Index)
    }
}
