package com.example.omnigo.features.customer.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UploadReviewImageResponse(
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("publicId")
    val publicId: String? = null
)
