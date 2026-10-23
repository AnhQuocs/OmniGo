package com.example.omnigo.core.network.dto

import com.google.gson.annotations.SerializedName

data class PageResponse<T>(
    @SerializedName("content")
    val content: List<T>? = null,
    @SerializedName("totalElements")
    val totalElements: Long? = null,
    @SerializedName("totalPages")
    val totalPages: Int? = null,
    @SerializedName("size")
    val size: Int? = null,
    @SerializedName("number")
    val number: Int? = null
)
