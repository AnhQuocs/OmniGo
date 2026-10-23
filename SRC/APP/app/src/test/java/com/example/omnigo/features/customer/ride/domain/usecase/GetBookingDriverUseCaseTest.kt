package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetBookingDriverUseCaseTest {

    private lateinit var rideBookingRepository: RideBookingRepository
    private lateinit var useCase: GetBookingDriverUseCase

    private val sampleDriverProfile = BookingDriverProfile(
        driverId = 77L,
        driverName = "Le Van C",
        driverPhone = "0988776655",
        vehiclePlate = "51F-999.88",
        vehicleType = "CAR_7",
        vehicleModel = "Mitsubishi Xpander",
        avatarUrl = "https://example.com/avatar77.png"
    )

    @Before
    fun setUp() {
        rideBookingRepository = mockk()
        useCase = GetBookingDriverUseCase(rideBookingRepository)
    }

    @Test
    fun `invoke with positive bookingId delegates to repository and returns success`() = runTest {
        coEvery {
            rideBookingRepository.getBookingDriver(123L)
        } returns GetBookingDriverResult.Success(sampleDriverProfile)

        val result = useCase(123L)

        assertTrue(result is GetBookingDriverResult.Success)
        val success = result as GetBookingDriverResult.Success
        assertEquals(77L, success.driverProfile.driverId)
        assertEquals("Le Van C", success.driverProfile.driverName)
        assertEquals("CAR_7", success.driverProfile.vehicleType)
        coVerify(exactly = 1) { rideBookingRepository.getBookingDriver(123L) }
    }

    @Test
    fun `invoke with non-positive bookingId returns BOOKING_NOT_FOUND error without repository call`() = runTest {
        val resultZero = useCase(0L)
        val resultNegative = useCase(-5L)

        assertTrue(resultZero is GetBookingDriverResult.Error)
        assertEquals(RideError.BOOKING_NOT_FOUND, (resultZero as GetBookingDriverResult.Error).error)

        assertTrue(resultNegative is GetBookingDriverResult.Error)
        assertEquals(RideError.BOOKING_NOT_FOUND, (resultNegative as GetBookingDriverResult.Error).error)

        coVerify(exactly = 0) { rideBookingRepository.getBookingDriver(any()) }
    }

    @Test
    fun `invoke when repository returns DRIVER_NOT_FOUND propagates error`() = runTest {
        coEvery {
            rideBookingRepository.getBookingDriver(456L)
        } returns GetBookingDriverResult.Error(
            error = RideError.DRIVER_NOT_FOUND,
            message = "Driver not assigned"
        )

        val result = useCase(456L)

        assertTrue(result is GetBookingDriverResult.Error)
        val error = result as GetBookingDriverResult.Error
        assertEquals(RideError.DRIVER_NOT_FOUND, error.error)
        assertEquals("Driver not assigned", error.message)
    }

    @Test
    fun `invoke when repository returns NETWORK_ERROR propagates error`() = runTest {
        coEvery {
            rideBookingRepository.getBookingDriver(789L)
        } returns GetBookingDriverResult.Error(
            error = RideError.NETWORK_ERROR,
            message = "Connection timeout"
        )

        val result = useCase(789L)

        assertTrue(result is GetBookingDriverResult.Error)
        val error = result as GetBookingDriverResult.Error
        assertEquals(RideError.NETWORK_ERROR, error.error)
        assertEquals("Connection timeout", error.message)
    }
}
