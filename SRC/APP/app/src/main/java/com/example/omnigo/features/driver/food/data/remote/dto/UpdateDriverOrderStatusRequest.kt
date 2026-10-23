package com.example.omnigo.features.driver.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateDriverOrderStatusRequest(
    @SerializedName("status")
    val status: String,
    @SerializedName("reason")
    val reason: String? = null,
    @SerializedName("reasonCode")
    val reasonCode: String? = null
)
