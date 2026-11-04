package com.example.omnigo.features.customer.ride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BookingReviewRequestDto(
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String? = null
)

data class BookingReviewResponseDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("bookingId") val bookingId: Long? = null,
    @SerializedName("customerId") val customerId: Long? = null,
    @SerializedName("driverId") val driverId: Long? = null,
    @SerializedName("rating") val rating: Int? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class DriverBookingReviewsEnvelopeDto(
    @SerializedName("driverId") val driverId: Long? = null,
    @SerializedName("totalReviews") val totalReviews: Int? = null,
    @SerializedName("averageRating") val averageRating: Double? = null,
    @SerializedName("reviews") val reviews: List<BookingReviewResponseDto>? = null
)
