package com.example.omnigo.features.driver.earnings.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WalletResponseDto(
    @SerializedName("walletId")
    val walletId: Long? = null,
    @SerializedName("balance")
    val balance: Double? = null,
    @SerializedName("userType")
    val userType: String? = null
)

