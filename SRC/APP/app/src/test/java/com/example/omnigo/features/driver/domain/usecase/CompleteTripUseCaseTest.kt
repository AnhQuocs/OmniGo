package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.CompleteRideBookingResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CompleteTripUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: CompleteTripUseCase

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = CompleteTripUseCase(rideBookingRepository)
    }

    @Test
    fun invoke_invalidBookingId_returnsError() = runTest {
        val result = useCase(0L, "CASH")

        assertTrue(result is CompleteRideBookingResult.Error)
        assertEquals(RideError.UNKNOWN_ERROR, (result as CompleteRideBookingResult.Error).error)
        coVerify(exactly = 0) { rideBookingRepository.completeBooking(any(), any()) }
    }

    @Test
    fun invoke_validBookingId_cash_success() = runTest {
        val booking = Booking(
            bookingId = 300L,
            status = BookingStatus.COMPLETED,
            price = 60000.0,
            paymentMethod = "CASH"
        )
        coEvery { rideBookingRepository.completeBooking(300L, "CASH") } returns CompleteRideBookingResult.Success(booking)

        val result = useCase(300L, "CASH")

        assertTrue(result is CompleteRideBookingResult.Success)
        assertEquals(BookingStatus.COMPLETED, (result as CompleteRideBookingResult.Success).booking.status)
        coVerify(exactly = 1) { rideBookingRepository.completeBooking(300L, "CASH") }
    }

    @Test
    fun invoke_validBookingId_app_success() = runTest {
        val booking = Booking(
            bookingId = 300L,
            status = BookingStatus.COMPLETED,
            price = 60000.0,
            paymentMethod = "APP"
        )
        coEvery { rideBookingRepository.completeBooking(300L, "APP") } returns CompleteRideBookingResult.Success(booking)

        val result = useCase(300L, "APP")

        assertTrue(result is CompleteRideBookingResult.Success)
        assertEquals("APP", (result as CompleteRideBookingResult.Success).booking.paymentMethod)
        coVerify(exactly = 1) { rideBookingRepository.completeBooking(300L, "APP") }
    }
}
