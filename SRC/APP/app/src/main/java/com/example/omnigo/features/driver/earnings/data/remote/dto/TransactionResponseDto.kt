package com.example.omnigo.features.driver.earnings.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TransactionResponseDto(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("walletId")
    val walletId: Long? = null,
    @SerializedName("bookingId")
    val bookingId: Long? = null,
    @SerializedName("orderId")
    val orderId: String? = null,
    @SerializedName("amount")
    val amount: Double? = null,
    @SerializedName("transactionType")
    val transactionType: String? = null,
    @SerializedName("paymentMethod")
    val paymentMethod: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null
)

