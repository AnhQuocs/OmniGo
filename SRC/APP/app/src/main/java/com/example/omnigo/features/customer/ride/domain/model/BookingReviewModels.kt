package com.example.omnigo.features.customer.ride.domain.model

data class BookingReview(
    val id: Long,
    val bookingId: Long,
    val customerId: Long,
    val driverId: Long,
    val rating: Int,
    val comment: String,
    val createdAt: String
)

data class DriverBookingReviewsSummary(
    val driverId: Long,
    val totalReviews: Int,
    val averageRating: Double,
    val reviews: List<BookingReview>
)
