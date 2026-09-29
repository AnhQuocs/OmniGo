package com.example.omnigo.features.customer.food.domain.model

data class CartState(
    val restaurantId: Long? = null,
    val restaurantName: String = "",
    val items: Map<Long, CartItem> = emptyMap()
) {
    val totalQuantity: Int
        get() = items.values.sumOf { it.quantity }

    val totalAmount: Double
        get() = items.values.sumOf { it.subtotalPrice }

    val isEmpty: Boolean
        get() = items.isEmpty()
}
