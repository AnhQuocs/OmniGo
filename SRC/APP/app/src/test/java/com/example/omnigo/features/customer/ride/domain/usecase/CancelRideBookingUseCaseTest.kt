package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CancelRideBookingUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: CancelRideBookingUseCase

    private val sampleCancelledBooking = Booking(
        bookingId = 777L,
        customerId = 15L,
        driverId = null,
        customerAvatarUrl = null,
        driverAvatarUrl = null,
        startLongitude = 105.8542,
        startLatitude = 21.0285,
        endLongitude = 105.8433,
        endLatitude = 21.0333,
        status = BookingStatus.CANCELLED,
        distanceInKm = 2.0,
        price = 30000.0,
        paymentMethod = "CASH"
    )

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = CancelRideBookingUseCase(rideBookingRepository)
    }

    @Test
    fun `invoke with positive bookingId delegates to repository and returns success`() = runTest {
        coEvery {
            rideBookingRepository.cancelBooking(777L)
        } returns CancelRideBookingResult.Success(sampleCancelledBooking)

        val result = useCase(777L)

        assertTrue(result is CancelRideBookingResult.Success)
        val success = result as CancelRideBookingResult.Success
        assertEquals(777L, success.booking.bookingId)
        assertEquals(BookingStatus.CANCELLED, success.booking.status)
        coVerify(exactly = 1) { rideBookingRepository.cancelBooking(777L) }
    }

    @Test
    fun `invoke with non-positive bookingId returns BOOKING_NOT_FOUND error without repository call`() = runTest {
        val resultZero = useCase(0L)
        val resultNegative = useCase(-10L)

        assertTrue(resultZero is CancelRideBookingResult.Error)
        assertEquals(RideError.BOOKING_NOT_FOUND, (resultZero as CancelRideBookingResult.Error).error)

        assertTrue(resultNegative is CancelRideBookingResult.Error)
        assertEquals(RideError.BOOKING_NOT_FOUND, (resultNegative as CancelRideBookingResult.Error).error)

        coVerify(exactly = 0) { rideBookingRepository.cancelBooking(any()) }
    }

    @Test
    fun `invoke when repository returns network error propagates error`() = runTest {
        coEvery {
            rideBookingRepository.cancelBooking(777L)
        } returns CancelRideBookingResult.Error(
            error = RideError.NETWORK_ERROR,
            message = "Timeout"
        )

        val result = useCase(777L)

        assertTrue(result is CancelRideBookingResult.Error)
        val error = result as CancelRideBookingResult.Error
        assertEquals(RideError.NETWORK_ERROR, error.error)
        assertEquals("Timeout", error.message)
    }
}
