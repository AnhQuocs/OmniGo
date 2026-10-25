package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.CompleteRideBookingResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import javax.inject.Inject

class CompleteTripUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository
) {
    suspend operator fun invoke(bookingId: Long, paymentMethod: String? = "CASH"): CompleteRideBookingResult {
        if (bookingId <= 0L) {
            return CompleteRideBookingResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = "Invalid booking ID: $bookingId"
            )
        }
        return rideBookingRepository.completeBooking(bookingId, paymentMethod)
    }
}
