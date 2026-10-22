package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderItemRequest
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderRequest
import com.example.omnigo.features.customer.food.domain.model.FoodOrderCreateCommand

fun FoodOrderCreateCommand.toRequest(): FoodOrderRequest =
    FoodOrderRequest(
        restaurantId = restaurantId,
        deliveryAddress = dropOffAddress,
        deliveryLatitude = dropOffLatitude,
        deliveryLongitude = dropOffLongitude,
        items = items.map { item ->
            FoodOrderItemRequest(
                menuItemId = item.menuItemId,
                quantity = item.quantity,
                note = item.note
            )
        },
        note = note,
        paymentMethod = paymentMethod.code
    )
