package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.CreateRideBookingResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateRideBookingUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: CreateRideBookingUseCase

    private val sampleBooking = Booking(
        bookingId = 555L,
        customerId = 10L,
        driverId = null,
        customerAvatarUrl = null,
        driverAvatarUrl = null,
        startLongitude = 105.8542,
        startLatitude = 21.0285,
        endLongitude = 105.8433,
        endLatitude = 21.0333,
        status = BookingStatus.PENDING,
        distanceInKm = 4.2,
        price = 60000.0,
        paymentMethod = "CASH"
    )

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = CreateRideBookingUseCase(rideBookingRepository)
    }

    @Test
    fun `invoke with valid coordinates delegates to repository and returns success`() = runTest {
        coEvery {
            rideBookingRepository.createBooking(
                startLatitude = 21.0285,
                startLongitude = 105.8542,
                endLatitude = 21.0333,
                endLongitude = 105.8433
            )
        } returns CreateRideBookingResult.Success(sampleBooking)

        val result = useCase(
            startLatitude = 21.0285,
            startLongitude = 105.8542,
            endLatitude = 21.0333,
            endLongitude = 105.8433
        )

        assertTrue(result is CreateRideBookingResult.Success)
        val success = result as CreateRideBookingResult.Success
        assertEquals(555L, success.booking.bookingId)
        assertEquals(BookingStatus.PENDING, success.booking.status)
        coVerify(exactly = 1) {
            rideBookingRepository.createBooking(21.0285, 105.8542, 21.0333, 105.8433)
        }
    }

    @Test
    fun `invoke with invalid latitude returns INVALID_COORDINATES error without calling repository`() = runTest {
        val result = useCase(
            startLatitude = 95.0, // Invalid latitude
            startLongitude = 105.8542,
            endLatitude = 21.0333,
            endLongitude = 105.8433
        )

        assertTrue(result is CreateRideBookingResult.Error)
        val error = result as CreateRideBookingResult.Error
        assertEquals(RideError.INVALID_COORDINATES, error.error)
        coVerify(exactly = 0) {
            rideBookingRepository.createBooking(any(), any(), any(), any())
        }
    }

    @Test
    fun `invoke with invalid longitude returns INVALID_COORDINATES error without calling repository`() = runTest {
        val result = useCase(
            startLatitude = 21.0285,
            startLongitude = 105.8542,
            endLatitude = 21.0333,
            endLongitude = -185.0 // Invalid longitude
        )

        assertTrue(result is CreateRideBookingResult.Error)
        val error = result as CreateRideBookingResult.Error
        assertEquals(RideError.INVALID_COORDINATES, error.error)
        coVerify(exactly = 0) {
            rideBookingRepository.createBooking(any(), any(), any(), any())
        }
    }

    @Test
    fun `invoke with NaN coordinates returns INVALID_COORDINATES error`() = runTest {
        val result = useCase(
            startLatitude = Double.NaN,
            startLongitude = 105.8542,
            endLatitude = 21.0333,
            endLongitude = 105.8433
        )

        assertTrue(result is CreateRideBookingResult.Error)
        val error = result as CreateRideBookingResult.Error
        assertEquals(RideError.INVALID_COORDINATES, error.error)
    }

    @Test
    fun `invoke when repository returns error propagates error properly`() = runTest {
        coEvery {
            rideBookingRepository.createBooking(any(), any(), any(), any())
        } returns CreateRideBookingResult.Error(
            error = RideError.SERVER_ERROR,
            message = "Internal Server Error"
        )

        val result = useCase(
            startLatitude = 21.0285,
            startLongitude = 105.8542,
            endLatitude = 21.0333,
            endLongitude = 105.8433
        )

        assertTrue(result is CreateRideBookingResult.Error)
        val error = result as CreateRideBookingResult.Error
        assertEquals(RideError.SERVER_ERROR, error.error)
        assertEquals("Internal Server Error", error.message)
    }
}
