package com.example.omnigo.features.customer.ride.domain.model

import com.example.omnigo.features.customer.ride.domain.error.RideError

sealed interface RejectRideBookingResult {
    data class Success(val booking: Booking) : RejectRideBookingResult
    data class Error(val error: RideError, val message: String? = null) : RejectRideBookingResult
}
