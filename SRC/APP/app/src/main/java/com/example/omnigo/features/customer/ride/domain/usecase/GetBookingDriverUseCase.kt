package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import javax.inject.Inject

class GetBookingDriverUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository
) {
    suspend operator fun invoke(bookingId: Long): GetBookingDriverResult {
        if (bookingId <= 0) {
            return GetBookingDriverResult.Error(
                error = RideError.BOOKING_NOT_FOUND,
                message = "Invalid bookingId"
            )
        }

        return rideBookingRepository.getBookingDriver(bookingId)
    }
}
