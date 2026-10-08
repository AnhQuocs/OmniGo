package com.example.omnigo.features.customer.food.domain.model

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int,
    val note: String = ""
) {
    val subtotalPrice: Double
        get() = menuItem.price * quantity
}
