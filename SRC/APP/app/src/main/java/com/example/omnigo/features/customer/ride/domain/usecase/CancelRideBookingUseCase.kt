package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import javax.inject.Inject

class CancelRideBookingUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository
) {
    suspend operator fun invoke(bookingId: Long): CancelRideBookingResult {
        if (bookingId <= 0) {
            return CancelRideBookingResult.Error(
                error = RideError.BOOKING_NOT_FOUND,
                message = "Invalid bookingId"
            )
        }

        return rideBookingRepository.cancelBooking(bookingId)
    }
}
