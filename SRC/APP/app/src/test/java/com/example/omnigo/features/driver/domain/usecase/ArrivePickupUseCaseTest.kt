package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.ArriveRideBookingResult
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

class ArrivePickupUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: ArrivePickupUseCase

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = ArrivePickupUseCase(rideBookingRepository)
    }

    @Test
    fun invoke_invalidBookingId_returnsErrorWithoutCallingRepository() = runTest {
        val result = useCase(0L)

        assertTrue(result is ArriveRideBookingResult.Error)
        assertEquals(RideError.UNKNOWN_ERROR, (result as ArriveRideBookingResult.Error).error)
        coVerify(exactly = 0) { rideBookingRepository.arriveBooking(any()) }
    }

    @Test
    fun invoke_validBookingId_success_returnsSuccess() = runTest {
        val booking = Booking(
            bookingId = 100L,
            status = BookingStatus.ARRIVED,
            price = 45000.0
        )
        coEvery { rideBookingRepository.arriveBooking(100L) } returns ArriveRideBookingResult.Success(booking)

        val result = useCase(100L)

        assertTrue(result is ArriveRideBookingResult.Success)
        assertEquals(BookingStatus.ARRIVED, (result as ArriveRideBookingResult.Success).booking.status)
        coVerify(exactly = 1) { rideBookingRepository.arriveBooking(100L) }
    }

    @Test
    fun invoke_repositoryReturnsError_returnsError() = runTest {
        coEvery { rideBookingRepository.arriveBooking(100L) } returns ArriveRideBookingResult.Error(
            error = RideError.NETWORK_ERROR,
            message = "No connection"
        )

        val result = useCase(100L)

        assertTrue(result is ArriveRideBookingResult.Error)
        assertEquals(RideError.NETWORK_ERROR, (result as ArriveRideBookingResult.Error).error)
    }
}
