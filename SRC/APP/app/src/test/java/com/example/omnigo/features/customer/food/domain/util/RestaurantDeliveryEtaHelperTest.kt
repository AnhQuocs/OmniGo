package com.example.omnigo.features.customer.food.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RestaurantDeliveryEtaHelperTest {

    @Test
    fun `estimate returns default range when coordinates are null or zero`() {
        val nullResult = RestaurantDeliveryEtaHelper.estimate(null, null, null, null)
        assertEquals(20, nullResult.minMinutes)
        assertEquals(30, nullResult.maxMinutes)
        assertNull(nullResult.distanceKm)

        val zeroResult = RestaurantDeliveryEtaHelper.estimate(0.0, 0.0, 21.015, 105.852)
        assertEquals(20, zeroResult.minMinutes)
        assertEquals(30, zeroResult.maxMinutes)
        assertNull(zeroResult.distanceKm)
    }

    @Test
    fun `estimate calculates valid range and distance for nearby restaurant`() {
        // Tọa độ gần nhau (~1km)
        val restaurantLat = 21.0285
        val restaurantLon = 105.8542
        val userLat = 21.0200
        val userLon = 105.8500

        val estimate = RestaurantDeliveryEtaHelper.estimate(
            restaurantLat = restaurantLat,
            restaurantLon = restaurantLon,
            userLat = userLat,
            userLon = userLon
        )

        assertNotNull(estimate.distanceKm)
        assertTrue(estimate.distanceKm!! in 0.5..2.0)
        assertTrue(estimate.minMinutes >= 15)
        assertEquals(estimate.minMinutes + 10, estimate.maxMinutes)
    }

    @Test
    fun `estimate increases eta for further distances`() {
        // Tọa độ xa hơn (~7-8km)
        val restaurantLat = 21.0800
        val restaurantLon = 105.8200
        val userLat = 21.0150
        val userLon = 105.8520

        val nearEstimate = RestaurantDeliveryEtaHelper.estimate(21.0285, 105.8542, 21.0200, 105.8500)
        val farEstimate = RestaurantDeliveryEtaHelper.estimate(restaurantLat, restaurantLon, userLat, userLon)

        assertTrue(farEstimate.distanceKm!! > nearEstimate.distanceKm!!)
        assertTrue(farEstimate.minMinutes >= nearEstimate.minMinutes)
    }

    @Test
    fun `formatEta returns clean minute range text`() {
        val etaText = RestaurantDeliveryEtaHelper.formatEta(21.0285, 105.8542, 21.0200, 105.8500)
        assertTrue(etaText.contains(" - "))
        assertTrue(etaText.endsWith("phút"))
    }

    @Test
    fun `formatEtaWithDistance includes distance suffix when coordinates are valid`() {
        val textWithDistance = RestaurantDeliveryEtaHelper.formatEtaWithDistance(
            restaurantLat = 21.0285,
            restaurantLon = 105.8542,
            userLat = 21.0200,
            userLon = 105.8500
        )
        assertTrue(textWithDistance.contains("phút • "))
        assertTrue(textWithDistance.endsWith("km"))
    }

    @Test
    fun `formatEtaWithDistance omits distance suffix when coordinates are missing`() {
        val textFallback = RestaurantDeliveryEtaHelper.formatEtaWithDistance(null, null, null, null)
        assertEquals("20 - 30 phút", textFallback)
    }
}
