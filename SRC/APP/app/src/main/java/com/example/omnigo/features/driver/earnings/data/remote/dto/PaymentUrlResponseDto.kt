package com.example.omnigo.features.driver.earnings.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaymentUrlResponseDto(
    @SerializedName("orderId")
    val orderId: String? = null,
    @SerializedName(value = "paymentUrl", alternate = ["payUrl"])
    val paymentUrl: String? = null,
    @SerializedName("status")
    val status: String? = null
)

data class PaymentStatusResponseDto(
    @SerializedName("bookingId")
    val bookingId: Long? = null,
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("paid")
    val paid: Boolean = false,
    @SerializedName("paymentStatus")
    val paymentStatus: String? = null,
    @SerializedName("paymentMethod")
    val paymentMethod: String? = null,
    @SerializedName("amount")
    val amount: Double? = null
)
