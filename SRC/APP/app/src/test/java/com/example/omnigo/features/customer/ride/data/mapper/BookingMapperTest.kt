package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookingMapperTest {

    private val gson = Gson()

    @Test
    fun `maps backend booking response correctly to domain entity`() {
        val json = """
            {
                "bookingId": 1001,
                "customerId": 88,
                "driverId": 42,
                "customerAvatarUrl": "https://example.com/customer.png",
                "driverAvatarUrl": "https://example.com/driver.png",
                "startLongitude": 105.8542,
                "startLatitude": 21.0285,
                "endLongitude": 105.8433,
                "endLatitude": 21.0333,
                "status": "PENDING",
                "distanceInKm": 3.5,
                "price": 45000.0,
                "paymentMethod": "CASH"
            }
        """.trimIndent()

        val response = gson.fromJson(json, BookingResponse::class.java)
        val domain = response.toDomain()

        assertEquals(1001L, domain.bookingId)
        assertEquals(88L, domain.customerId)
        assertEquals(42L, domain.driverId)
        assertEquals("https://example.com/customer.png", domain.customerAvatarUrl)
        assertEquals("https://example.com/driver.png", domain.driverAvatarUrl)
        assertEquals(105.8542, domain.startLongitude, 0.0001)
        assertEquals(21.0285, domain.startLatitude, 0.0001)
        assertEquals(105.8433, domain.endLongitude, 0.0001)
        assertEquals(21.0333, domain.endLatitude, 0.0001)
        assertEquals(BookingStatus.PENDING, domain.status)
        assertEquals(3.5, domain.distanceInKm, 0.001)
        assertEquals(45000.0, domain.price, 0.001)
        assertEquals("CASH", domain.paymentMethod)
    }

    @Test
    fun `handles null and unrecognized status safely defaulting to PENDING`() {
        val response = BookingResponse(
            bookingId = null,
            customerId = null,
            driverId = null,
            status = "UNKNOWN_STATUS_XYZ"
        )

        val domain = response.toDomain()

        assertEquals(0L, domain.bookingId)
        assertEquals(0L, domain.customerId)
        assertNull(domain.driverId)
        assertEquals(BookingStatus.PENDING, domain.status)
    }
}
