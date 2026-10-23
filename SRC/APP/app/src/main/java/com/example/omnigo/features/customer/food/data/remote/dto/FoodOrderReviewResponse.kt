package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FoodOrderReviewResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("restaurantRating")
    val restaurantRating: Int? = null,
    @SerializedName("restaurantComment")
    val restaurantComment: String? = null,
    @SerializedName("driverRating")
    val driverRating: Int? = null,
    @SerializedName("driverTags")
    val driverTags: List<String>? = null,
    @SerializedName("reviewPhotos")
    val reviewPhotos: List<String>? = null,
    @SerializedName("merchantReply")
    val merchantReply: String? = null,
    @SerializedName("merchantReplyAt")
    val merchantReplyAt: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("readOnly")
    val readOnly: Boolean? = null
)
