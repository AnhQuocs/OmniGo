package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.CreateRideBookingResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import javax.inject.Inject

class CreateRideBookingUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository
) {
    suspend operator fun invoke(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
        vehicleType: String? = null,
        pickupAddress: String? = null,
        dropoffAddress: String? = null
    ): CreateRideBookingResult {
        if (!isValidCoordinate(startLatitude, startLongitude) || !isValidCoordinate(endLatitude, endLongitude)) {
            return CreateRideBookingResult.Error(
                error = RideError.INVALID_COORDINATES,
                message = "Invalid coordinate values"
            )
        }

        return rideBookingRepository.createBooking(
            startLatitude = startLatitude,
            startLongitude = startLongitude,
            endLatitude = endLatitude,
            endLongitude = endLongitude,
            vehicleType = vehicleType,
            pickupAddress = pickupAddress,
            dropoffAddress = dropoffAddress
        )
    }

    private fun isValidCoordinate(latitude: Double, longitude: Double): Boolean {
        if (latitude.isNaN() || longitude.isNaN()) return false
        if (latitude < -90.0 || latitude > 90.0) return false
        if (longitude < -180.0 || longitude > 180.0) return false
        return true
    }
}
