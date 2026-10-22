package com.example.omnigo.features.driver.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.FoodOfferEventDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DriverFoodOfferMapperTest {

    @Test
    fun `mapToDomain with valid dto maps all fields correctly`() {
        val dto = FoodOfferEventDto(
            orderId = 123L,
            customerId = 456L,
            restaurantId = 789L,
            restaurantName = " Bun Cha Ha Noi ",
            restaurantAddress = " 1 Hang Manh ",
            restaurantLatitude = 21.031,
            restaurantLongitude = 105.849,
            dropOffAddress = " 2 Ngo Tram ",
            dropOffLatitude = 21.032,
            dropOffLongitude = 105.850,
            deliveryFee = 25000.0,
            totalPrice = 120000.0,
            paymentMethod = "CASH",
            isPaid = false
        )

        val domain = DriverFoodOfferMapper.mapToDomain(dto)

        assertNotNull(domain)
        domain!!
        assertEquals(123L, domain.orderId)
        assertEquals(456L, domain.customerId)
        assertEquals(789L, domain.restaurantId)
        assertEquals("Bun Cha Ha Noi", domain.restaurantName)
        assertEquals("1 Hang Manh", domain.restaurantAddress)
        assertEquals(21.031, domain.restaurantLatitude!!, 0.0001)
        assertEquals(105.849, domain.restaurantLongitude!!, 0.0001)
        assertEquals("2 Ngo Tram", domain.dropOffAddress)
        assertEquals(21.032, domain.dropOffLatitude!!, 0.0001)
        assertEquals(105.850, domain.dropOffLongitude!!, 0.0001)
        assertEquals(25000.0, domain.deliveryFee, 0.001)
        assertEquals(120000.0, domain.totalPrice, 0.001)
        assertEquals("CASH", domain.paymentMethod)
        assertEquals(false, domain.isPaid)
        assertEquals(20, domain.timeoutSeconds)
    }

    @Test
    fun `mapToDomain with null orderId returns null`() {
        val dto = FoodOfferEventDto(
            orderId = null,
            restaurantName = "Pizza"
        )
        assertNull(DriverFoodOfferMapper.mapToDomain(dto))
    }

    @Test
    fun `mapToDomain with non-positive orderId returns null`() {
        val dto = FoodOfferEventDto(
            orderId = -1L,
            restaurantName = "Pizza"
        )
        assertNull(DriverFoodOfferMapper.mapToDomain(dto))

        val dtoZero = FoodOfferEventDto(
            orderId = 0L,
            restaurantName = "Pizza"
        )
        assertNull(DriverFoodOfferMapper.mapToDomain(dtoZero))
    }
}
