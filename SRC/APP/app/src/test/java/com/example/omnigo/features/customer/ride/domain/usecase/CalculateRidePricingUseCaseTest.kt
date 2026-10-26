package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.CalculateRidePricingResult
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate
import com.example.omnigo.features.customer.ride.domain.repository.PricingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateRidePricingUseCaseTest {

    private lateinit var pricingRepository: PricingRepository
    private lateinit var useCase: CalculateRidePricingUseCase

    private val sampleEstimate = PricingEstimate(
        baseFare = 15000.0,
        distanceFare = 30000.0,
        durationFare = 5000.0,
        surgeMultiplier = 1.0,
        surgeLevel = "NORMAL",
        totalPrice = 50000.0,
        distanceInKm = 5.0,
        estimatedDurationMinutes = 15,
        h3Index = "h3_sample"
    )

    @Before
    fun setUp() {
        pricingRepository = mockk()
        useCase = CalculateRidePricingUseCase(pricingRepository)
    }

    @Test
    fun `invoke with valid coordinates delegates to repository and returns success`() = runTest {
        coEvery {
            pricingRepository.calculatePricing(
                startLatitude = 21.0285,
                startLongitude = 105.8542,
                endLatitude = 21.0333,
                endLongitude = 105.8433,
                nearbyDriversCount = 2,
                vehicleType = "BIKE"
            )
        } returns CalculateRidePricingResult.Success(sampleEstimate)

        val result = useCase(
            startLatitude = 21.0285,
            startLongitude = 105.8542,
            endLatitude = 21.0333,
            endLongitude = 105.8433,
            nearbyDriversCount = 2,
            vehicleType = "BIKE"
        )

        assertTrue(result is CalculateRidePricingResult.Success)
        val success = result as CalculateRidePricingResult.Success
        assertEquals(50000.0, success.estimate.totalPrice, 0.001)
        assertEquals(5.0, success.estimate.distanceInKm, 0.001)
        coVerify(exactly = 1) {
            pricingRepository.calculatePricing(21.0285, 105.8542, 21.0333, 105.8433, 2, "BIKE")
        }
    }

    @Test
    fun `invoke with invalid start latitude returns INVALID_COORDINATES error without calling repository`() = runTest {
        val result = useCase(
            startLatitude = 95.0, // Invalid latitude (> 90)
            startLongitude = 105.8542,
            endLatitude = 21.0333,
            endLongitude = 105.8433
        )

        assertTrue(result is CalculateRidePricingResult.Error)
        val error = result as CalculateRidePricingResult.Error
        assertEquals(RideError.INVALID_COORDINATES, error.error)
        coVerify(exactly = 0) {
            pricingRepository.calculatePricing(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `invoke with invalid end longitude returns INVALID_COORDINATES error without calling repository`() = runTest {
        val result = useCase(
            startLatitude = 21.0285,
            startLongitude = 105.8542,
            endLatitude = 21.0333,
            endLongitude = 195.0 // Invalid longitude (> 180)
        )

        assertTrue(result is CalculateRidePricingResult.Error)
        val error = result as CalculateRidePricingResult.Error
        assertEquals(RideError.INVALID_COORDINATES, error.error)
        coVerify(exactly = 0) {
            pricingRepository.calculatePricing(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `invoke with NaN coordinate returns INVALID_COORDINATES error`() = runTest {
        val result = useCase(
            startLatitude = Double.NaN,
            startLongitude = 105.8542,
            endLatitude = 21.0333,
            endLongitude = 105.8433
        )

        assertTrue(result is CalculateRidePricingResult.Error)
        val error = result as CalculateRidePricingResult.Error
        assertEquals(RideError.INVALID_COORDINATES, error.error)
        coVerify(exactly = 0) {
            pricingRepository.calculatePricing(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `invoke when repository returns network error propagates error`() = runTest {
        coEvery {
            pricingRepository.calculatePricing(any(), any(), any(), any(), any(), any())
        } returns CalculateRidePricingResult.Error(
            error = RideError.NETWORK_ERROR,
            message = "No connection"
        )

        val result = useCase(
            startLatitude = 21.0285,
            startLongitude = 105.8542,
            endLatitude = 21.0333,
            endLongitude = 105.8433
        )

        assertTrue(result is CalculateRidePricingResult.Error)
        val error = result as CalculateRidePricingResult.Error
        assertEquals(RideError.NETWORK_ERROR, error.error)
        assertEquals("No connection", error.message)
    }
}
