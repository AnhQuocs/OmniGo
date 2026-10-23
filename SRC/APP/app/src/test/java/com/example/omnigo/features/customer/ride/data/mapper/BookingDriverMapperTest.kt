package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.BookingDriverResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookingDriverMapperTest {

    private val gson = Gson()

    @Test
    fun `maps backend booking driver response json correctly to domain model`() {
        val json = """
            {
                "driverId": 42,
                "driverName": "Nguyen Van A",
                "driverPhone": "0912345678",
                "vehiclePlate": "29A-123.45",
                "vehicleType": "CAR_4",
                "vehicleModel": "Toyota Vios 2022",
                "avatarUrl": "https://example.com/avatar42.jpg"
            }
        """.trimIndent()

        val response = gson.fromJson(json, BookingDriverResponse::class.java)
        val domain = response.toDomain()

        assertEquals(42L, domain.driverId)
        assertEquals("Nguyen Van A", domain.driverName)
        assertEquals("0912345678", domain.driverPhone)
        assertEquals("29A-123.45", domain.vehiclePlate)
        assertEquals("CAR_4", domain.vehicleType)
        assertEquals("Toyota Vios 2022", domain.vehicleModel)
        assertEquals("https://example.com/avatar42.jpg", domain.avatarUrl)
    }

    @Test
    fun `falls back to requestedDriverId when driverId in response is null`() {
        val response = BookingDriverResponse(
            driverId = null,
            driverName = "Tran Van B",
            driverPhone = null,
            vehiclePlate = null,
            vehicleType = null,
            vehicleModel = null,
            avatarUrl = null
        )

        val domain = response.toDomain(requestedDriverId = 99L)

        assertEquals(99L, domain.driverId)
        assertEquals("Tran Van B", domain.driverName)
        assertNull(domain.driverPhone)
        assertNull(domain.vehiclePlate)
        assertNull(domain.vehicleType)
        assertNull(domain.vehicleModel)
        assertNull(domain.avatarUrl)
    }
}
