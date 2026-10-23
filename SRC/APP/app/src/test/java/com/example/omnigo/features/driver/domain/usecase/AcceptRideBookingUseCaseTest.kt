package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.AcceptRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AcceptRideBookingUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: AcceptRideBookingUseCase

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = AcceptRideBookingUseCase(rideBookingRepository)
    }

    @Test
    fun `invoke with valid bookingId calls repository and returns success`() = runTest {
        val bookingId = 100L
        val mockBooking = Booking(
            bookingId = bookingId,
            customerId = 1L,
            driverId = 2L,
            customerAvatarUrl = null,
            driverAvatarUrl = null,
            startLatitude = 10.7,
            startLongitude = 106.7,
            endLatitude = 10.8,
            endLongitude = 106.8,
            status = BookingStatus.ACCEPTED,
            distanceInKm = 4.0,
            price = 55000.0,
            paymentMethod = "CASH"
        )
        coEvery { rideBookingRepository.acceptBooking(bookingId) } returns AcceptRideBookingResult.Success(mockBooking)

        val result = useCase(bookingId)

        assertTrue(result is AcceptRideBookingResult.Success)
        assertEquals(bookingId, (result as AcceptRideBookingResult.Success).booking.bookingId)
        assertEquals(BookingStatus.ACCEPTED, result.booking.status)
        coVerify(exactly = 1) { rideBookingRepository.acceptBooking(bookingId) }
    }

    @Test
    fun `invoke with non-positive bookingId returns Error without calling repository`() = runTest {
        val zeroResult = useCase(0L)
        assertTrue(zeroResult is AcceptRideBookingResult.Error)
        assertEquals(RideError.UNKNOWN_ERROR, (zeroResult as AcceptRideBookingResult.Error).error)

        val negativeResult = useCase(-10L)
        assertTrue(negativeResult is AcceptRideBookingResult.Error)
        assertEquals(RideError.UNKNOWN_ERROR, (negativeResult as AcceptRideBookingResult.Error).error)

        coVerify(exactly = 0) { rideBookingRepository.acceptBooking(any()) }
    }

    @Test
    fun `invoke propagates Error from repository`() = runTest {
        val bookingId = 200L
        coEvery { rideBookingRepository.acceptBooking(bookingId) } returns AcceptRideBookingResult.Error(
            error = RideError.BOOKING_NOT_FOUND,
            message = "Booking not found"
        )

        val result = useCase(bookingId)

        assertTrue(result is AcceptRideBookingResult.Error)
        assertEquals(RideError.BOOKING_NOT_FOUND, (result as AcceptRideBookingResult.Error).error)
    }
}
