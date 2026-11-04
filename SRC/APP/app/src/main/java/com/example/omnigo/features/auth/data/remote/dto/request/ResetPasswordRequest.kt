package com.example.omnigo.features.auth.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("verificationToken")
    val verificationToken: String,
    @SerializedName("newPassword")
    val newPassword: String,
    @SerializedName("confirmPassword")
    val confirmPassword: String
)

