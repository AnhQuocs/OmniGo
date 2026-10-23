package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateFoodReviewRequest(
    @SerializedName("orderId")
    val orderId: Long,
    @SerializedName("restaurantRating")
    val restaurantRating: Int,
    @SerializedName("restaurantComment")
    val restaurantComment: String? = null,
    @SerializedName("driverRating")
    val driverRating: Int,
    @SerializedName("driverTags")
    val driverTags: List<String>? = null,
    @SerializedName("reviewPhotos")
    val reviewPhotos: List<String>? = null
)
