package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FoodOrderReviewResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("customerId")
    val customerId: Long? = null,
    @SerializedName("customerName")
    val customerName: String? = null,
    @SerializedName("restaurantId")
    val restaurantId: Long? = null,
    @SerializedName("driverId")
    val driverId: Long? = null,
    @SerializedName("restaurantRating")
    val restaurantRating: Int? = null,
    @SerializedName("restaurantComment")
    val restaurantComment: String? = null,
    @SerializedName("driverRating")
    val driverRating: Int? = null,
    @SerializedName("driverComment")
    val driverComment: String? = null,
    @SerializedName("driverTags")
    val driverTags: List<String>? = null,
    @SerializedName(value = "restaurantImages", alternate = ["reviewPhotos", "images"])
    val restaurantImages: List<String>? = null,
    @SerializedName("merchantReply")
    val merchantReply: String? = null,
    @SerializedName("merchantReplyAt")
    val merchantReplyAt: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("canEdit")
    val canEdit: Boolean? = null,
    @SerializedName("readOnly")
    val readOnly: Boolean? = null
)

data class RestaurantReviewsEnvelopeDto(
    @SerializedName("restaurantId") val restaurantId: Long? = null,
    @SerializedName("totalReviews") val totalReviews: Int? = null,
    @SerializedName("averageRating") val averageRating: Double? = null,
    @SerializedName(value = "reviews", alternate = ["content"]) val reviews: List<FoodOrderReviewResponse>? = null
)

data class DriverFoodReviewsEnvelopeDto(
    @SerializedName("driverId") val driverId: Long? = null,
    @SerializedName("totalReviews") val totalReviews: Int? = null,
    @SerializedName("averageRating") val averageRating: Double? = null,
    @SerializedName("reviews") val reviews: List<FoodOrderReviewResponse>? = null
)
