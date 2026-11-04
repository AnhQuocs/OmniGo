package com.example.omnigo.features.customer.address.domain.model

data class SavedAddress(
    val id: Long,
    val userId: Long,
    val label: String,
    val addressType: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean
)

