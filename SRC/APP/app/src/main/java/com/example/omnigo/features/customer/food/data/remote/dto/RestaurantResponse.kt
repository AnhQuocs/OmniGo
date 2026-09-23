package com.example.omnigo.features.customer.food.data.remote.dto

data class RestaurantResponse(
    val id: Long,
    val ownerId: Long?,
    val name: String,
    val phone: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val imageUrl: String?,
    val status: String?,
    val openTime: String?,
    val closeTime: String?,
    val rating: Double?,
    val reviewCount: Int?,
    val totalReviews: Int?,
    val isLocked: Boolean?,
    val lockedReason: String?,
    val lockedAt: String?,
    val menuItems: List<MenuItemResponse>?,
    val createdAt: String?,
    val updatedAt: String?
)
