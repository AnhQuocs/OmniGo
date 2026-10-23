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
    val isReadOnly: Boolean
)
