package com.example.omnigo.features.customer.ride.domain.model

import com.example.omnigo.features.customer.ride.domain.error.RideError

sealed interface ArriveRideBookingResult {
    data class Success(val booking: Booking) : ArriveRideBookingResult
    data class Error(val error: RideError, val message: String? = null) : ArriveRideBookingResult
}
