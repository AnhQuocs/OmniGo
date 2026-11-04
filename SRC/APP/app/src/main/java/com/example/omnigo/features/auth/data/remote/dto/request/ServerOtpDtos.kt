package com.example.omnigo.features.auth.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class SendServerOtpRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    @SerializedName("email")
    val email: String? = null
)

data class VerifyServerOtpRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("otp")
    val otp: String
)

data class VerifyServerOtpResponse(
    @SerializedName("verificationToken")
    val verificationToken: String? = null
)

data class FcmTokenRequest(
    @SerializedName("fcmToken")
    val fcmToken: String
)
