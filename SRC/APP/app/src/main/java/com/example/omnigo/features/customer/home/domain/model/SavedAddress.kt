package com.example.omnigo.features.customer.home.domain.model

data class SavedAddress(
    val id: String,
    val addressName: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double,
    val provinceId: String = "",
    val districtId: String = "",
    val wardId: String = "",
    val streetDetail: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
