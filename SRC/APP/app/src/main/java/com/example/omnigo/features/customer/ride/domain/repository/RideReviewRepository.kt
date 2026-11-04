package com.example.omnigo.features.customer.ride.domain.repository

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingReview
import com.example.omnigo.features.customer.ride.domain.model.DriverBookingReviewsSummary

interface RideReviewRepository {
    suspend fun getBookingDetail(bookingId: Long): Result<Booking>
    suspend fun submitBookingReview(
        bookingId: Long,
        rating: Int,
        comment: String?
    ): Result<BookingReview>
    suspend fun getBookingReview(bookingId: Long): Result<BookingReview?>
    suspend fun getDriverBookingReviews(driverId: Long): Result<DriverBookingReviewsSummary>
}
