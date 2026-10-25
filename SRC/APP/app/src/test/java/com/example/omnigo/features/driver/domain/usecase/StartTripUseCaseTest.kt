package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.StartRideBookingResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartTripUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: StartTripUseCase

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = StartTripUseCase(rideBookingRepository)
    }

    @Test
    fun invoke_invalidBookingId_returnsErrorWithoutCallingRepository() = runTest {
        val result = useCase(-1L)

        assertTrue(result is StartRideBookingResult.Error)
        assertEquals(RideError.UNKNOWN_ERROR, (result as StartRideBookingResult.Error).error)
        coVerify(exactly = 0) { rideBookingRepository.startBooking(any()) }
    }

    @Test
    fun invoke_validBookingId_success_returnsSuccess() = runTest {
        val booking = Booking(
            bookingId = 200L,
            status = BookingStatus.IN_PROGRESS,
            price = 55000.0
        )
        coEvery { rideBookingRepository.startBooking(200L) } returns StartRideBookingResult.Success(booking)

        val result = useCase(200L)

        assertTrue(result is StartRideBookingResult.Success)
        assertEquals(BookingStatus.IN_PROGRESS, (result as StartRideBookingResult.Success).booking.status)
        coVerify(exactly = 1) { rideBookingRepository.startBooking(200L) }
    }

    @Test
    fun invoke_repositoryError_returnsError() = runTest {
        coEvery { rideBookingRepository.startBooking(200L) } returns StartRideBookingResult.Error(
            error = RideError.SERVER_ERROR,
            message = "Server internal failure"
        )

        val result = useCase(200L)

        assertTrue(result is StartRideBookingResult.Error)
        assertEquals(RideError.SERVER_ERROR, (result as StartRideBookingResult.Error).error)
    }
}
