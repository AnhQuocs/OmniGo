package com.example.omnigo.features.customer.food.data.remote.dto

data class MenuItemResponse(
    val id: Long,
    val restaurantId: Long,
    val name: String,
    val description: String?,
    val price: Double,
    val imageUrl: String?,
    val category: String?,
    val isAvailable: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)
