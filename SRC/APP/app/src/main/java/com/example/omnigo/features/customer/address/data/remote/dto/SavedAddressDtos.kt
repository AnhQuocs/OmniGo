package com.example.omnigo.features.customer.address.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SaveAddressRequestDto(
    @SerializedName("label")
    val label: String,
    @SerializedName("addressType")
    val addressType: String = "OTHER",
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("isDefault")
    val isDefault: Boolean = false
)

data class SavedAddressResponseDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("label")
    val label: String,
    @SerializedName("addressType")
    val addressType: String?,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("isDefault")
    val isDefault: Boolean?
)

