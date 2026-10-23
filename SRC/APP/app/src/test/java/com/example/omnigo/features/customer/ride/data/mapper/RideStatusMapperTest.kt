package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RideStatusMapperTest {

    @Test
    fun `toRideStatusEvent with valid BookingResponse maps correctly`() {
        val dto = BookingResponse(
            bookingId = 123L,
            status = "ACCEPTED",
            driverId = 456L,
            price = 65000.0,
            paymentMethod = "CASH"
        )

        val event = dto.toRideStatusEvent()

        assertNotNull(event)
        assertEquals(123L, event?.bookingId)
        assertEquals(BookingStatus.ACCEPTED, event?.status)
        assertEquals(456L, event?.driverId)
        assertEquals(65000.0, event?.price ?: 0.0, 0.001)
        assertEquals("CASH", event?.paymentMethod)
    }

    @Test
    fun `toRideStatusEvent with null or non-positive bookingId returns null`() {
        val nullIdDto = BookingResponse(bookingId = null, status = "ACCEPTED")
        assertNull(nullIdDto.toRideStatusEvent())

        val zeroIdDto = BookingResponse(bookingId = 0L, status = "ACCEPTED")
        assertNull(zeroIdDto.toRideStatusEvent())

        val negativeIdDto = BookingResponse(bookingId = -10L, status = "ACCEPTED")
        assertNull(negativeIdDto.toRideStatusEvent())
    }

    @Test
    fun `toRideStatusEvent with blank or invalid status returns null`() {
        val blankStatusDto = BookingResponse(bookingId = 1L, status = "  ")
        assertNull(blankStatusDto.toRideStatusEvent())

        val invalidStatusDto = BookingResponse(bookingId = 1L, status = "UNKNOWN_CUSTOM_STATUS")
        assertNull(invalidStatusDto.toRideStatusEvent())
    }
}
