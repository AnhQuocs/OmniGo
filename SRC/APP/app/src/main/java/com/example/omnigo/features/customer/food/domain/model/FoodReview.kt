package com.example.omnigo.features.customer.food.domain.model

data class FoodReview(
    val id: Long,
    val orderId: Long,
    val restaurantRating: Int,
    val restaurantComment: String?,
    val driverRating: Int,
    val driverTags: List<String>,
    val reviewPhotos: List<String>,
    val merchantReply: String?,
    val merchantReplyAt: String?,
    val createdAt: String?,
    val isReadOnly: Boolean,
    val customerId: Long = 0L,
    val customerName: String? = null,
    val driverComment: String? = null,
    val updatedAt: String? = null,
    val canEdit: Boolean = false
)

data class DriverFoodReviewsSummary(
    val driverId: Long,
    val totalReviews: Int,
    val averageRating: Double,
    val reviews: List<FoodReview>
)
