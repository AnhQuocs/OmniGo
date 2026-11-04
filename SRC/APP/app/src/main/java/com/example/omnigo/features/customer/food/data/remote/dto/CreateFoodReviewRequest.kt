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
    @SerializedName("driverComment")
    val driverComment: String? = null,
    @SerializedName("images")
    val images: List<String>? = null
)
