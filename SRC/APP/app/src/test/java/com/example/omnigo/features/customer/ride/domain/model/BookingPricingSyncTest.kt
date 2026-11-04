package com.example.omnigo.features.customer.ride.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookingPricingSyncTest {

    @Test
    fun `booking without voucher discount computes finalPrice equal to price`() {
        val booking = Booking(
            bookingId = 101L,
            price = 35000.0,
            discountAmount = 0.0,
            voucherCode = null
        )

        assertEquals(35000.0, booking.price, 0.001)
        assertEquals(35000.0, booking.finalPrice, 0.001)
        assertNull(booking.voucherCode)
    }

    @Test
    fun `booking with voucher discount computes finalPrice subtracted by discountAmount`() {
        val booking = Booking(
            bookingId = 102L,
            price = 35000.0,
            discountAmount = 10000.0,
            voucherCode = "OMNIGO10K",
            originalPrice = 35000.0
        )

        assertEquals(35000.0, booking.price, 0.001)
        assertEquals(10000.0, booking.discountAmount, 0.001)
        assertEquals(25000.0, booking.finalPrice, 0.001)
        assertEquals("OMNIGO10K", booking.voucherCode)
        assertEquals(35000.0, booking.originalPrice ?: 0.0, 0.001)
    }

    @Test
    fun `booking with voucher discount larger than price coerces finalPrice to zero`() {
        val booking = Booking(
            bookingId = 103L,
            price = 20000.0,
            discountAmount = 30000.0,
            voucherCode = "FREERIDE"
        )

        assertEquals(0.0, booking.finalPrice, 0.001)
    }

    @Test
    fun `driver earnings remains based on trip price regardless of customer voucher`() {
        val booking = Booking(
            bookingId = 104L,
            price = 50000.0,
            platformFee = 10000.0,
            discountAmount = 20000.0,
            voucherCode = "BIGDISCOUNT"
        )

        // Customer pays 30.000 đ
        assertEquals(30000.0, booking.finalPrice, 0.001)
        // Driver still receives fare minus platform fee = 40.000 đ
        assertEquals(40000.0, booking.actualEarnings, 0.001)
        assertEquals(10000.0, booking.actualPlatformFee, 0.001)
    }

    @Test
    fun `activeRideState correctly calculates finalPrice with and without discount`() {
        val stateWithoutVoucher = ActiveRideState(
            bookingId = 201L,
            price = 45000.0,
            discountAmount = 0.0
        )
        assertEquals(45000.0, stateWithoutVoucher.finalPrice, 0.001)

        val stateWithVoucher = ActiveRideState(
            bookingId = 202L,
            price = 45000.0,
            discountAmount = 15000.0,
            voucherCode = "SAVE15K"
        )
        assertEquals(30000.0, stateWithVoucher.finalPrice, 0.001)
        assertEquals("SAVE15K", stateWithVoucher.voucherCode)
    }
}

