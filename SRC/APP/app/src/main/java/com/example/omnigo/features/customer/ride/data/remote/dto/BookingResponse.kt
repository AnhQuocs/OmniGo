package com.example.omnigo.features.customer.ride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BookingResponse(
    @SerializedName(value = "bookingId", alternate = ["id"])
    val bookingId: Long? = null,
    @SerializedName("customerId")
    val customerId: Long? = null,
    @SerializedName("driverId")
    val driverId: Long? = null,
    @SerializedName("customerAvatarUrl")
    val customerAvatarUrl: String? = null,
    @SerializedName(value = "customerName", alternate = ["userName", "fullName", "name"])
    val customerName: String? = null,
    @SerializedName(value = "customerPhone", alternate = ["phoneNumber", "phone"])
    val customerPhone: String? = null,
    @SerializedName("driverAvatarUrl")
    val driverAvatarUrl: String? = null,
    @SerializedName("pickupAddress")
    val pickupAddress: String? = null,
    @SerializedName("dropoffAddress")
    val dropoffAddress: String? = null,
    @SerializedName("startLongitude")
    val startLongitude: Double? = null,
    @SerializedName("startLatitude")
    val startLatitude: Double? = null,
    @SerializedName("endLongitude")
    val endLongitude: Double? = null,
    @SerializedName("endLatitude")
    val endLatitude: Double? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("distanceInKm")
    val distanceInKm: Double? = null,
    @SerializedName("price")
    val price: Double? = null,
    @SerializedName("vehicleType")
    val vehicleType: String? = null,
    @SerializedName("paymentMethod")
    val paymentMethod: String? = null
)
