package com.example.omnigo.features.customer.ride.domain.model

import com.example.omnigo.features.customer.ride.domain.error.RideError

sealed interface CompleteRideBookingResult {
    data class Success(val booking: Booking) : CompleteRideBookingResult
    data class Error(val error: RideError, val message: String? = null) : CompleteRideBookingResult
}
