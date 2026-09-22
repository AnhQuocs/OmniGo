package com.example.omnigo.core.region.data.dto

import com.google.gson.annotations.SerializedName

data class ProvinceDto(
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Name") val name: String? = null,
    @SerializedName("Districts") val districts: List<DistrictDto>? = null
)

data class DistrictDto(
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Name") val name: String? = null,
    @SerializedName("Wards") val wards: List<WardDto>? = null
)

data class WardDto(
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Name") val name: String? = null,
    @SerializedName("Level") val level: String? = null
)

