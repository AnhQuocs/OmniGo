package com.example.omnigo.features.driver.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DriverStatusUpdateRequest(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("isOnline")
    val isOnline: Boolean? = null
)
