package com.example.omnigo.features.customer.food.domain.model

data class FoodOrderCreateCommand(
    val restaurantId: Long,
    val dropOffAddress: String,
    val dropOffLatitude: Double,
    val dropOffLongitude: Double,
    val items: List<FoodOrderCreateItemCommand>,
    val note: String?,
    val paymentMethod: PaymentMethod
)

data class FoodOrderCreateItemCommand(
    val menuItemId: Long,
    val quantity: Int,
    val note: String?
)
