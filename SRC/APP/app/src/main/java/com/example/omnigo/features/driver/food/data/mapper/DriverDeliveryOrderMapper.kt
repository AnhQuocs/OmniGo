package com.example.omnigo.features.driver.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderItemResponse
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrderItem
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus

object DriverDeliveryOrderMapper {

    fun mapToDomain(dto: FoodOrderResponse): DriverDeliveryOrder? {
        val orderId = dto.id ?: return null
        if (orderId <= 0L) return null

        val mappedItems = dto.items?.map { mapItem(it) }.orEmpty()

        return DriverDeliveryOrder(
            orderId = orderId,
            customerId = dto.customerId,
            customerName = dto.customerName?.trim().orEmpty(),
            customerPhone = dto.customerPhone?.trim().orEmpty(),
            restaurantId = dto.restaurantId,
            restaurantName = dto.restaurantName?.trim().orEmpty(),
            restaurantAddress = dto.restaurantAddress?.trim().orEmpty(),
            restaurantLatitude = dto.restaurantLatitude,
            restaurantLongitude = dto.restaurantLongitude,
            dropOffAddress = dto.dropOffAddress?.trim().orEmpty(),
            dropOffLatitude = dto.dropOffLatitude,
            dropOffLongitude = dto.dropOffLongitude,
            status = DriverDeliveryStatus.fromString(dto.status),
            paymentMethod = dto.paymentMethod ?: "CASH",
            isPaid = dto.isPaid ?: false,
            itemsPrice = dto.itemsPrice ?: 0.0,
            deliveryFee = dto.deliveryFee ?: 0.0,
            totalPrice = dto.totalPrice ?: 0.0,
            items = mappedItems,
            note = dto.note?.trim(),
            estimatedDeliveryMinutes = dto.estimatedDeliveryMinutes,
            createdAt = dto.createdAt
        )
    }

    private fun mapItem(dto: FoodOrderItemResponse): DriverDeliveryOrderItem {
        val price = dto.itemPrice ?: 0.0
        val qty = dto.quantity.coerceAtLeast(1)
        val subtotal = dto.subtotal ?: (price * qty)

        return DriverDeliveryOrderItem(
            menuItemId = dto.menuItemId,
            itemName = dto.itemName?.trim().orEmpty(),
            quantity = qty,
            itemPrice = price,
            subtotal = subtotal,
            note = dto.note?.trim()
        )
    }

    fun mapFromOffer(offer: DriverFoodOffer): DriverDeliveryOrder {
        return DriverDeliveryOrder(
            orderId = offer.orderId,
            customerId = offer.customerId,
            restaurantId = offer.restaurantId,
            restaurantName = offer.restaurantName,
            restaurantAddress = offer.restaurantAddress,
            restaurantLatitude = offer.restaurantLatitude,
            restaurantLongitude = offer.restaurantLongitude,
            dropOffAddress = offer.dropOffAddress,
            dropOffLatitude = offer.dropOffLatitude,
            dropOffLongitude = offer.dropOffLongitude,
            status = DriverDeliveryStatus.ACCEPTED,
            paymentMethod = offer.paymentMethod ?: if (offer.isPaid) "ONLINE" else "CASH",
            isPaid = offer.isPaid,
            itemsPrice = (offer.totalPrice - offer.deliveryFee).coerceAtLeast(0.0),
            deliveryFee = offer.deliveryFee,
            totalPrice = offer.totalPrice,
            items = emptyList()
        )
    }
}
