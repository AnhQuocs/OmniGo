package com.example.omnigo.features.driver.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.FoodOfferEventDto
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer

object DriverFoodOfferMapper {

    fun mapToDomain(dto: FoodOfferEventDto): DriverFoodOffer? {
        val orderId = dto.orderId ?: return null
        if (orderId <= 0L) return null

        return DriverFoodOffer(
            orderId = orderId,
            customerId = dto.customerId,
            restaurantId = dto.restaurantId,
            restaurantName = dto.restaurantName?.trim().orEmpty(),
            restaurantAddress = dto.restaurantAddress?.trim().orEmpty(),
            restaurantLatitude = dto.restaurantLatitude,
            restaurantLongitude = dto.restaurantLongitude,
            dropOffAddress = dto.dropOffAddress?.trim().orEmpty(),
            dropOffLatitude = dto.dropOffLatitude,
            dropOffLongitude = dto.dropOffLongitude,
            deliveryFee = dto.deliveryFee ?: 0.0,
            totalPrice = dto.totalPrice ?: 0.0,
            isPaid = dto.isPaid ?: false,
            paymentMethod = dto.paymentMethod,
            timeoutSeconds = DriverFoodOffer.DEFAULT_TIMEOUT_SECONDS
        )
    }
}
