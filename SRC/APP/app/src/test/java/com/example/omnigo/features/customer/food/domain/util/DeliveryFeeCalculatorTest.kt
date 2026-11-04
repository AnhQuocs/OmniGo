package com.example.omnigo.features.customer.food.domain.util

import com.example.omnigo.features.customer.food.domain.model.Restaurant
import org.junit.Assert.assertEquals
import org.junit.Test

class DeliveryFeeCalculatorTest {

    private fun createRestaurant(lat: Double, lng: Double): Restaurant {
        return Restaurant(
            id = 1L,
            name = "Test Restaurant",
            phone = "0900000000",
            address = "Hà Nội",
            latitude = lat,
            longitude = lng,
            imageUrl = "",
            status = "OPEN",
            openTime = "08:00",
            closeTime = "22:00",
            rating = 5.0,
            reviewCount = 10,
            isOpen = true,
            menuItems = emptyList()
        )
    }

    @Test
    fun `when coordinates are missing or restaurant is null, returns 0`() {
        val fee1 = DeliveryFeeCalculator.calculate(null, 21.0, 105.0)
        assertEquals(0.0, fee1, 0.001)

        val restaurant = createRestaurant(0.0, 0.0)
        val fee2 = DeliveryFeeCalculator.calculate(restaurant, 21.0, 105.0)
        assertEquals(0.0, fee2, 0.001)

        val validRestaurant = createRestaurant(21.0285, 105.8542)
        val fee3 = DeliveryFeeCalculator.calculate(validRestaurant, 0.0, 0.0)
        assertEquals(0.0, fee3, 0.001)
    }

    @Test
    fun `when distance is under 50m, returns 0 bypass`() {
        // Less than 50m apart (0.0001 lat is ~11 meters)
        val restaurant = createRestaurant(21.02850, 105.85420)
        val fee = DeliveryFeeCalculator.calculate(restaurant, 21.02852, 105.85422)
        assertEquals(0.0, fee, 0.001)
    }

    @Test
    fun `when road distance is within 2km, returns base fee 15000`() {
        // Air distance ~1km -> road distance ~1.35km <= 2km
        val restaurant = createRestaurant(21.0285, 105.8542)
        // 0.009 lat is ~1.0 km
        val fee = DeliveryFeeCalculator.calculate(restaurant, 21.0375, 105.8542)
        assertEquals(15000.0, fee, 0.001)
    }

    @Test
    fun `when road distance is between 2km and 5km, adds 5000 per extra km`() {
        // Air distance ~2.5km -> road distance ~3.375km (extra 1.375km -> ceil = 2km)
        // fee = 15000 + 2 * 5000 = 25000
        val restaurant = createRestaurant(21.0285, 105.8542)
        val fee = DeliveryFeeCalculator.calculate(restaurant, 21.0510, 105.8542)
        assertEquals(25000.0, fee, 0.001)
    }

    @Test
    fun `when road distance exceeds 5km, adds 4000 per km above 5km`() {
        // Air distance ~4.5km -> road distance ~6.075km (tier2 = 15000, extra tier3 = 1.075km -> ceil = 2km)
        // fee = 15000 + 15000 + 2 * 4000 = 38000
        val restaurant = createRestaurant(21.0285, 105.8542)
        val fee = DeliveryFeeCalculator.calculate(restaurant, 21.0690, 105.8542)
        assertEquals(38000.0, fee, 0.001)
    }
}
