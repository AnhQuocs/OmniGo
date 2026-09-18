package com.example.omnigo.features.customer.food.domain.model

data class MenuItem(
    val id: Long,
    val restaurantId: Long,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val isAvailable: Boolean
)
