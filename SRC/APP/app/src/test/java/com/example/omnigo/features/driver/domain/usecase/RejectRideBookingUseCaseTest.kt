package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.RejectRideBookingResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RejectRideBookingUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: RejectRideBookingUseCase

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = RejectRideBookingUseCase(rideBookingRepository)
    }

    @Test
    fun `invoke with valid bookingId calls repository and returns success`() = runTest {
        val bookingId = 100L
        val mockBooking = Booking(
            bookingId = bookingId,
            customerId = 1L,
            driverId = null,
            customerAvatarUrl = null,
            driverAvatarUrl = null,
            startLatitude = 10.7,
            startLongitude = 106.7,
            endLatitude = 10.8,
            endLongitude = 106.8,
            status = BookingStatus.CANCELLED,
            distanceInKm = 4.0,
            price = 55000.0,
            paymentMethod = "CASH"
        )
        coEvery { rideBookingRepository.rejectBooking(bookingId) } returns RejectRideBookingResult.Success(mockBooking)

        val result = useCase(bookingId)

        assertTrue(result is RejectRideBookingResult.Success)
        assertEquals(bookingId, (result as RejectRideBookingResult.Success).booking.bookingId)
        coVerify(exactly = 1) { rideBookingRepository.rejectBooking(bookingId) }
    }

    @Test
    fun `invoke with non-positive bookingId returns Error without calling repository`() = runTest {
        val zeroResult = useCase(0L)
        assertTrue(zeroResult is RejectRideBookingResult.Error)
        assertEquals(RideError.UNKNOWN_ERROR, (zeroResult as RejectRideBookingResult.Error).error)

        val negativeResult = useCase(-10L)
        assertTrue(negativeResult is RejectRideBookingResult.Error)
        assertEquals(RideError.UNKNOWN_ERROR, (negativeResult as RejectRideBookingResult.Error).error)

        coVerify(exactly = 0) { rideBookingRepository.rejectBooking(any()) }
    }

    @Test
    fun `invoke propagates Error from repository`() = runTest {
        val bookingId = 200L
        coEvery { rideBookingRepository.rejectBooking(bookingId) } returns RejectRideBookingResult.Error(
            error = RideError.SERVER_ERROR,
            message = "Server error"
        )

        val result = useCase(bookingId)

        assertTrue(result is RejectRideBookingResult.Error)
        assertEquals(RideError.SERVER_ERROR, (result as RejectRideBookingResult.Error).error)
    }
}
