package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderItemResponse
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod

fun FoodOrderItemResponse.toDomain(): FoodOrderItem {
    val unitPrice = itemPrice ?: 0.0
    val itemSubtotal = subtotal ?: (unitPrice * quantity)
    return FoodOrderItem(
        id = id ?: 0L,
        menuItemId = menuItemId,
        itemName = itemName.orEmpty(),
        itemPrice = unitPrice,
        quantity = quantity,
        subtotal = itemSubtotal,
        note = note.orEmpty()
    )
}

fun FoodOrderResponse.toDomain(): FoodOrder {
    val itemsMapped = items?.map { it.toDomain() }.orEmpty()
    val calculatedItemsPrice = itemsPrice ?: itemsMapped.sumOf { it.subtotal }
    val calcDeliveryFee = deliveryFee ?: 0.0
    val calcTotalPrice = totalPrice ?: (calculatedItemsPrice + calcDeliveryFee)

    return FoodOrder(
        id = orderId ?: id ?: 0L,
        customerId = customerId,
        driverId = driverId,
        restaurantId = restaurantId ?: 0L,
        restaurantName = restaurantName.orEmpty(),
        status = status.orEmpty(),
        paymentMethod = PaymentMethod.fromCode(paymentMethod),
        isPaid = isPaid ?: false,
        itemsPrice = calculatedItemsPrice,
        deliveryFee = calcDeliveryFee,
        totalPrice = calcTotalPrice,
        dropOffAddress = dropOffAddress.orEmpty(),
        dropOffLatitude = dropOffLatitude ?: 0.0,
        dropOffLongitude = dropOffLongitude ?: 0.0,
        note = note.orEmpty(),
        items = itemsMapped,
        estimatedDeliveryMinutes = estimatedDeliveryMinutes ?: 25,
        createdAt = createdAt.orEmpty()
    )
}
