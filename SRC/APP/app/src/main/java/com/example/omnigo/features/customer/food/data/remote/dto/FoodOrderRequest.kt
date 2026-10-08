package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FoodOrderItemRequest(
    @SerializedName("menuItemId")
    val menuItemId: Long,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("note")
    val note: String? = null
)

data class FoodOrderRequest(
    @SerializedName("restaurantId")
    val restaurantId: Long,
    @SerializedName("dropOffAddress")
    val dropOffAddress: String,
    @SerializedName("dropOffLatitude")
    val dropOffLatitude: Double,
    @SerializedName("dropOffLongitude")
    val dropOffLongitude: Double,
    @SerializedName("items")
    val items: List<FoodOrderItemRequest>,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("paymentMethod")
    val paymentMethod: String
)
