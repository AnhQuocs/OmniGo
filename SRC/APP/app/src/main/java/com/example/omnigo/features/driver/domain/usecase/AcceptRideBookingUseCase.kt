package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.AcceptRideBookingResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import javax.inject.Inject

class AcceptRideBookingUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository
) {
    suspend operator fun invoke(bookingId: Long): AcceptRideBookingResult {
        if (bookingId <= 0L) {
            return AcceptRideBookingResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = "Invalid booking ID: $bookingId"
            )
        }
        return rideBookingRepository.acceptBooking(bookingId)
    }
}
