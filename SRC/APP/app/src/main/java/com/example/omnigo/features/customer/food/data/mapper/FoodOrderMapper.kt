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

    return FoodOrder(
        id = id ?: 0L,
        customerId = customerId,
        driverId = driverId,
        driverName = driverName,
        driverPhone = driverPhone,
        driverVehiclePlate = driverVehiclePlate,
        driverAvatarUrl = driverAvatarUrl,
        restaurantId = restaurantId ?: 0L,
        restaurantName = restaurantName.orEmpty(),
        restaurantAddress = restaurantAddress,
        status = status.orEmpty(),
        paymentMethod = PaymentMethod.fromCode(paymentMethod),
        isPaid = isPaid,
        itemsPrice = calculatedItemsPrice,
        deliveryFee = deliveryFee,
        totalPrice = totalPrice,

        dropOffAddress = dropOffAddress.orEmpty(),

        dropOffLatitude = dropOffLatitude,
        dropOffLongitude = dropOffLongitude,

        note = note.orEmpty(),

        items = itemsMapped,

        estimatedDeliveryMinutes = estimatedDeliveryMinutes,

        createdAt = createdAt.orEmpty()

    )

}
