package com.example.omnigo.features.customer.ride.domain.model

import com.example.omnigo.features.customer.ride.domain.error.RideError

sealed interface StartRideBookingResult {
    data class Success(val booking: Booking) : StartRideBookingResult
    data class Error(val error: RideError, val message: String? = null) : StartRideBookingResult
}
