package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FoodOrderItemResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("menuItemId")
    val menuItemId: Long,
    @SerializedName("itemName")
    val itemName: String? = null,
    @SerializedName("itemPrice")
    val itemPrice: Double? = null,
    @SerializedName("quantity")
    val quantity: Int = 1,
    @SerializedName("subtotal")
    val subtotal: Double? = null,
    @SerializedName("note")
    val note: String? = null
)

data class FoodOrderResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("customerId")
    val customerId: Long? = null,
    @SerializedName("driverId")
    val driverId: Long? = null,
    @SerializedName("restaurantId")
    val restaurantId: Long? = null,
    @SerializedName("restaurantName")
    val restaurantName: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("paymentMethod")
    val paymentMethod: String? = null,
    @SerializedName("isPaid")
    val isPaid: Boolean? = null,
    @SerializedName("itemsPrice")
    val itemsPrice: Double? = null,
    @SerializedName("deliveryFee")
    val deliveryFee: Double? = null,
    @SerializedName("totalPrice")
    val totalPrice: Double? = null,
    @SerializedName("dropOffAddress")
    val dropOffAddress: String? = null,
    @SerializedName("dropOffLatitude")
    val dropOffLatitude: Double? = null,
    @SerializedName("dropOffLongitude")
    val dropOffLongitude: Double? = null,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("items")
    val items: List<FoodOrderItemResponse>? = null,
    @SerializedName("estimatedDeliveryMinutes")
    val estimatedDeliveryMinutes: Int? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null
)
