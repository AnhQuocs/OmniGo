package com.example.omnigo.features.customer.food.domain.model

sealed interface CartAddResult {
    data object Success : CartAddResult
    data object ItemUnavailable : CartAddResult
    data class RestaurantConflict(
        val currentRestaurantId: Long,
        val currentRestaurantName: String,
        val newRestaurantId: Long,
        val newRestaurantName: String,
        val pendingItem: MenuItem,
        val pendingQuantity: Int = 1,
        val pendingNote: String = ""
    ) : CartAddResult
}
