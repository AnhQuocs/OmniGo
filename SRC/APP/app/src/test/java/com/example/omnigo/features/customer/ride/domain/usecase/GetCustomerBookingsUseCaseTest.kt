package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetCustomerBookingsUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: GetCustomerBookingsUseCase

    private val sampleBookings = listOf(
        Booking(
            bookingId = 101L,
            customerId = 10L,
            status = BookingStatus.IN_PROGRESS,
            startLatitude = 21.0,
            startLongitude = 105.0,
            endLatitude = 21.1,
            endLongitude = 105.1,
            distanceInKm = 3.5,
            price = 45000.0,
            paymentMethod = "CASH"
        ),
        Booking(
            bookingId = 102L,
            customerId = 10L,
            status = BookingStatus.COMPLETED,
            startLatitude = 21.0,
            startLongitude = 105.0,
            endLatitude = 21.2,
            endLongitude = 105.2,
            distanceInKm = 5.0,
            price = 60000.0,
            paymentMethod = "APP"
        )
    )

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = GetCustomerBookingsUseCase(rideBookingRepository)
    }

    @Test
    fun `invoke delegates to repository and returns success with booking list`() = runTest {
        coEvery {
            rideBookingRepository.getCustomerBookings()
        } returns GetCustomerBookingsResult.Success(sampleBookings)

        val result = useCase()

        assertTrue(result is GetCustomerBookingsResult.Success)
        val success = result as GetCustomerBookingsResult.Success
        assertEquals(2, success.bookings.size)
        assertEquals(101L, success.bookings[0].bookingId)
        assertEquals(102L, success.bookings[1].bookingId)
        coVerify(exactly = 1) { rideBookingRepository.getCustomerBookings() }
    }

    @Test
    fun `invoke when repository returns error propagates error`() = runTest {
        coEvery {
            rideBookingRepository.getCustomerBookings()
        } returns GetCustomerBookingsResult.Error(
            error = RideError.NETWORK_ERROR,
            message = "Network unavailable"
        )

        val result = useCase()

        assertTrue(result is GetCustomerBookingsResult.Error)
        val error = result as GetCustomerBookingsResult.Error
        assertEquals(RideError.NETWORK_ERROR, error.error)
        assertEquals("Network unavailable", error.message)
        coVerify(exactly = 1) { rideBookingRepository.getCustomerBookings() }
    }
}
