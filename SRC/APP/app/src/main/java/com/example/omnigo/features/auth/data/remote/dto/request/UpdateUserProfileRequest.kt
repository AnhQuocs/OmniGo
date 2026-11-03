package com.example.omnigo.features.auth.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class UpdateUserProfileRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String?
)

