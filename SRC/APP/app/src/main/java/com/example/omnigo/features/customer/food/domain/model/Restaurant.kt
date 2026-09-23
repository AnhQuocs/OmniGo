package com.example.omnigo.features.customer.food.domain.model

data class Restaurant(
    val id: Long,
    val name: String,
    val phone: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val status: String,
    val openTime: String,
    val closeTime: String,
    val rating: Double,
    val reviewCount: Int,
    val isOpen: Boolean,
    val menuItems: List<MenuItem>
)
