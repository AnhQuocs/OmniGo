package com.example.omnigo.features.customer.ride.domain.model

import com.example.omnigo.features.customer.ride.domain.error.RideError

sealed interface AcceptRideBookingResult {
    data class Success(val booking: Booking) : AcceptRideBookingResult
    data class Error(val error: RideError, val message: String? = null) : AcceptRideBookingResult
}
