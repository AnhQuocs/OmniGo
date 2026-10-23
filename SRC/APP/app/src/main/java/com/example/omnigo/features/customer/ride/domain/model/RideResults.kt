package com.example.omnigo.features.customer.ride.domain.model

import com.example.omnigo.features.customer.ride.domain.error.RideError

sealed interface CreateRideBookingResult {
    data class Success(
        val booking: Booking
    ) : CreateRideBookingResult

    data class Error(
        val error: RideError,
        val message: String? = null
    ) : CreateRideBookingResult
}

sealed interface CancelRideBookingResult {
    data class Success(
        val booking: Booking
    ) : CancelRideBookingResult

    data class Error(
        val error: RideError,
        val message: String? = null
    ) : CancelRideBookingResult
}

sealed interface CalculateRidePricingResult {
    data class Success(
        val estimate: PricingEstimate
    ) : CalculateRidePricingResult

    data class Error(
        val error: RideError,
        val message: String? = null
    ) : CalculateRidePricingResult
}

sealed interface GetBookingDriverResult {
    data class Success(
        val driverProfile: BookingDriverProfile
    ) : GetBookingDriverResult

    data class Error(
        val error: RideError,
        val message: String? = null
    ) : GetBookingDriverResult
}
