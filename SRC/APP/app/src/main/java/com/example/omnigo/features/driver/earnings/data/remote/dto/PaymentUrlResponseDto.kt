package com.example.omnigo.features.driver.earnings.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaymentUrlResponseDto(
    @SerializedName("orderId")
    val orderId: String? = null,
    @SerializedName("paymentUrl")
    val paymentUrl: String? = null
)

