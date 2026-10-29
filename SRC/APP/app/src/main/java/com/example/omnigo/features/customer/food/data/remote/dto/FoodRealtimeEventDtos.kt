package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FoodOrderStatusEventDto(
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null
)

data class FoodOrderAssignmentEventDto(
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("driverId")
    val driverId: Long? = null,
    @SerializedName("driverName")
    val driverName: String? = null,
    @SerializedName("driverPhone")
    val driverPhone: String? = null,
    @SerializedName(value = "vehiclePlate", alternate = ["licensePlate"])
    val vehiclePlate: String? = null
)

data class DriverLocationEventDto(
    @SerializedName("driverId")
    val driverId: Long? = null,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("bearing")
    val bearing: Double? = null,
    @SerializedName("speed")
    val speed: Double? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null
)

data class FoodOfferEventDto(
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("customerId")
    val customerId: Long? = null,
    @SerializedName(value = "customerName", alternate = ["userName", "fullName", "name"])
    val customerName: String? = null,
    @SerializedName(value = "customerPhone", alternate = ["phoneNumber", "phone"])
    val customerPhone: String? = null,
    @SerializedName("restaurantId")
    val restaurantId: Long? = null,
    @SerializedName("restaurantName")
    val restaurantName: String? = null,
    @SerializedName("restaurantAddress")
    val restaurantAddress: String? = null,
    @SerializedName("restaurantLatitude")
    val restaurantLatitude: Double? = null,
    @SerializedName("restaurantLongitude")
    val restaurantLongitude: Double? = null,
    @SerializedName(value = "dropOffAddress", alternate = ["deliveryAddress"])
    val dropOffAddress: String? = null,
    @SerializedName(value = "dropOffLatitude", alternate = ["deliveryLatitude"])
    val dropOffLatitude: Double? = null,
    @SerializedName(value = "dropOffLongitude", alternate = ["deliveryLongitude"])
    val dropOffLongitude: Double? = null,
    @SerializedName("deliveryFee")
    val deliveryFee: Double? = null,
    @SerializedName("totalPrice")
    val totalPrice: Double? = null,
    @SerializedName("paymentMethod")
    val paymentMethod: String? = null,
    @SerializedName("isPaid")
    val isPaid: Boolean? = null
)
