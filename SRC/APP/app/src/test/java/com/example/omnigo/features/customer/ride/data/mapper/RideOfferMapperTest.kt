package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RideOfferMapperTest {

    @Test
    fun `toRideOfferEvent maps valid pending booking response correctly`() {
        val dto = BookingResponse(
            bookingId = 42L,
            customerId = 10L,
            startLatitude = 10.762622,
            startLongitude = 106.660172,
            endLatitude = 10.772622,
            endLongitude = 106.670172,
            pickupAddress = " 123 Le Loi, Q1 ",
            dropoffAddress = " 456 Nguyen Hue, Q1 ",
            status = "PENDING",
            distanceInKm = 3.5,
            price = 45000.0,
            paymentMethod = "CASH"
        )

        val event = dto.toRideOfferEvent()

        assertNotNull(event)
        assertEquals(42L, event?.bookingId)
        assertEquals(10.762622, event?.pickupLat ?: 0.0, 0.000001)
        assertEquals(106.660172, event?.pickupLng ?: 0.0, 0.000001)
        assertEquals(10.772622, event?.dropoffLat ?: 0.0, 0.000001)
        assertEquals(106.670172, event?.dropoffLng ?: 0.0, 0.000001)
        assertEquals("123 Le Loi, Q1", event?.pickupAddress)
        assertEquals("456 Nguyen Hue, Q1", event?.dropoffAddress)
        assertEquals(3.5, event?.distanceInKm ?: 0.0, 0.001)
        assertEquals(45000.0, event?.price ?: 0.0, 0.001)
        assertEquals("CASH", event?.paymentMethod)
        assertEquals(BookingStatus.PENDING, event?.status)
        assertEquals(10L, event?.customerId)
    }

    @Test
    fun `toRideOfferEvent maps cancelled booking response even with null coordinates`() {
        val dto = BookingResponse(
            bookingId = 99L,
            status = "CANCELLED"
        )

        val event = dto.toRideOfferEvent()

        assertNotNull(event)
        assertEquals(99L, event?.bookingId)
        assertEquals(BookingStatus.CANCELLED, event?.status)
    }

    @Test
    fun `toRideOfferEvent returns null when bookingId is null or non-positive`() {
        val nullIdDto = BookingResponse(
            bookingId = null,
            startLatitude = 10.0,
            startLongitude = 106.0,
            endLatitude = 10.1,
            endLongitude = 106.1
        )
        assertNull(nullIdDto.toRideOfferEvent())

        val zeroIdDto = BookingResponse(
            bookingId = 0L,
            startLatitude = 10.0,
            startLongitude = 106.0,
            endLatitude = 10.1,
            endLongitude = 106.1
        )
        assertNull(zeroIdDto.toRideOfferEvent())

        val negativeIdDto = BookingResponse(
            bookingId = -5L,
            startLatitude = 10.0,
            startLongitude = 106.0,
            endLatitude = 10.1,
            endLongitude = 106.1
        )
        assertNull(negativeIdDto.toRideOfferEvent())
    }

    @Test
    fun `toRideOfferEvent returns null when coordinates are invalid or out of range`() {
        val outOfRangeLat = BookingResponse(
            bookingId = 1L,
            startLatitude = 95.0, // Invalid > 90
            startLongitude = 106.0,
            endLatitude = 10.0,
            endLongitude = 106.0
        )
        assertNull(outOfRangeLat.toRideOfferEvent())

        val outOfRangeLng = BookingResponse(
            bookingId = 1L,
            startLatitude = 10.0,
            startLongitude = -190.0, // Invalid < -180
            endLatitude = 10.0,
            endLongitude = 106.0
        )
        assertNull(outOfRangeLng.toRideOfferEvent())

        val missingEndCoord = BookingResponse(
            bookingId = 1L,
            startLatitude = 10.0,
            startLongitude = 106.0,
            endLatitude = null,
            endLongitude = 106.0
        )
        assertNull(missingEndCoord.toRideOfferEvent())
    }
}
