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
    @SerializedName("deliveryAddress")
    val deliveryAddress: String,
    @SerializedName("deliveryLatitude")
    val deliveryLatitude: Double,
    @SerializedName("deliveryLongitude")
    val deliveryLongitude: Double,
    @SerializedName("items")
    val items: List<FoodOrderItemRequest>,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("paymentMethod")
    val paymentMethod: String
)
