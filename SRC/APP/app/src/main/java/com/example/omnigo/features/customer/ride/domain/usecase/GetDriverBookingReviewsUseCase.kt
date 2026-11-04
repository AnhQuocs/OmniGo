package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.DriverBookingReviewsSummary
import com.example.omnigo.features.customer.ride.domain.repository.RideReviewRepository
import javax.inject.Inject

class GetDriverBookingReviewsUseCase @Inject constructor(
    private val rideReviewRepository: RideReviewRepository
) {
    suspend operator fun invoke(driverId: Long): Result<DriverBookingReviewsSummary> {
        return rideReviewRepository.getDriverBookingReviews(driverId)
    }
}
