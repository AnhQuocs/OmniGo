package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.DriverLocationEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderAssignmentEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderStatusEventDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FoodRealtimeEventMapperTest {
    @Test
    fun `maps nullable order status contract`() {
        assertEquals(
            com.example.omnigo.features.customer.food.domain.model.FoodOrderStatusEvent(
                orderId = 42L,
                status = null,
                timestamp = null
            ),
            FoodOrderStatusEventDto(orderId = 42L).toDomain()
        )
    }

    @Test
    fun `rejects status event without a valid order id`() {
        assertNull(FoodOrderStatusEventDto(orderId = 0L).toDomain())
    }

    @Test
    fun `maps assignment payload`() {
        val event = FoodOrderAssignmentEventDto(
            orderId = 42L,
            driverId = 9L,
            driverName = "Driver",
            driverPhone = "0900000000",
            vehiclePlate = "ABC-123"
        ).toDomain()

        assertEquals(42L, event?.orderId)
        assertEquals(9L, event?.driverId)
        assertEquals("ABC-123", event?.vehiclePlate)
    }

    @Test
    fun `rejects location when coordinates are missing or out of bounds`() {
        assertNull(DriverLocationEventDto(driverId = 9L, latitude = null, longitude = 2.0).toDomain())
        assertNull(DriverLocationEventDto(driverId = 9L, latitude = 91.0, longitude = 2.0).toDomain())
    }

    @Test
    fun `maps gps payload and leaves optional telemetry nullable`() {
        val location = DriverLocationEventDto(
            driverId = 9L,
            latitude = 21.0,
            longitude = 105.0
        ).toDomain()

        assertEquals(21.0, location?.latitude)
        assertEquals(105.0, location?.longitude)
        assertEquals(null, location?.bearing)
        assertEquals(null, location?.speed)
        assertEquals(null, location?.timestamp)
    }
}
