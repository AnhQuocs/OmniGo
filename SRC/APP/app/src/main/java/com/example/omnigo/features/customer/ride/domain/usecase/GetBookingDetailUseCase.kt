package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.repository.RideReviewRepository
import javax.inject.Inject

class GetBookingDetailUseCase @Inject constructor(
    private val rideReviewRepository: RideReviewRepository
) {
    suspend operator fun invoke(bookingId: Long): Result<Booking> {
        return rideReviewRepository.getBookingDetail(bookingId)
    }
}
