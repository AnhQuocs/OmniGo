package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.NearbyDriver
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import javax.inject.Inject

class GetNearbyDriversUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        radius: Double = 5.0
    ): List<NearbyDriver> {
        if (latitude.isNaN() || longitude.isNaN()) return emptyList()
        return rideBookingRepository.getNearbyDrivers(
            latitude = latitude,
            longitude = longitude,
            radius = radius
        )
    }
}
