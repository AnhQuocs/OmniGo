package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.CalculateRidePricingResult
import com.example.omnigo.features.customer.ride.domain.repository.PricingRepository
import javax.inject.Inject

class CalculateRidePricingUseCase @Inject constructor(
    private val pricingRepository: PricingRepository
) {
    suspend operator fun invoke(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
        nearbyDriversCount: Int = 0
    ): CalculateRidePricingResult {
        if (!isValidCoordinate(startLatitude, startLongitude) || !isValidCoordinate(endLatitude, endLongitude)) {
            return CalculateRidePricingResult.Error(
                error = RideError.INVALID_COORDINATES,
                message = "Invalid coordinate values"
            )
        }

        return pricingRepository.calculatePricing(
            startLatitude = startLatitude,
            startLongitude = startLongitude,
            endLatitude = endLatitude,
            endLongitude = endLongitude,
            nearbyDriversCount = nearbyDriversCount
        )
    }

    private fun isValidCoordinate(latitude: Double, longitude: Double): Boolean {
        if (latitude.isNaN() || longitude.isNaN()) return false
        if (latitude < -90.0 || latitude > 90.0) return false
        if (longitude < -180.0 || longitude > 180.0) return false
        return true
    }
}
