package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.BookingReview
import com.example.omnigo.features.customer.ride.domain.repository.RideReviewRepository
import javax.inject.Inject

class GetBookingReviewUseCase @Inject constructor(
    private val rideReviewRepository: RideReviewRepository
) {
    suspend operator fun invoke(bookingId: Long): Result<BookingReview?> {
        return rideReviewRepository.getBookingReview(bookingId)
    }
}
