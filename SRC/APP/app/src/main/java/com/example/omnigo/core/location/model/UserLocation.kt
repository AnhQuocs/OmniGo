package com.example.omnigo.core.location.model

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val addressName: String = "",
    val fullAddress: String = ""
)
