package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.BookingReview
import com.example.omnigo.features.customer.ride.domain.repository.RideReviewRepository
import javax.inject.Inject

class SubmitBookingReviewUseCase @Inject constructor(
    private val rideReviewRepository: RideReviewRepository
) {
    suspend operator fun invoke(
        bookingId: Long,
        rating: Int,
        comment: String?
    ): Result<BookingReview> {
        return rideReviewRepository.submitBookingReview(bookingId, rating, comment)
    }
}
