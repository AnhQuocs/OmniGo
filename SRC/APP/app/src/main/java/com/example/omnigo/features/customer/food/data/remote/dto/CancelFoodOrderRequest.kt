package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CancelFoodOrderRequest(
    @SerializedName("reason")
    val reason: String? = null,
    @SerializedName("reasonCode")
    val reasonCode: String? = null
)
