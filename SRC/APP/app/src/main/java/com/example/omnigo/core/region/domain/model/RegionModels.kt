package com.example.omnigo.core.region.domain.model

data class Province(
    val id: String,
    val name: String,
    val districts: List<District> = emptyList()
)

data class District(
    val id: String,
    val name: String,
    val wards: List<Ward> = emptyList()
)

data class Ward(
    val id: String,
    val name: String,
    val level: String = ""
)

data class SelectedAddress(
    val province: Province,
    val district: District,
    val ward: Ward,
    val streetDetail: String = "",
    val fullFormattedAddress: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
