package com.example.omnigo.features.customer.food.domain.model

data class CreateFoodReviewCommand(
    val orderId: Long,
    val restaurantRating: Int,
    val restaurantComment: String? = null,
    val driverRating: Int,
    val driverTags: List<String> = emptyList(),
    val reviewPhotos: List<String> = emptyList()
)
