package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import com.example.omnigo.features.customer.food.domain.util.DeliveryFeeCalculator
import javax.inject.Inject

class CalculateFoodDeliveryFeeUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(
        restaurant: Restaurant?,
        customerLat: Double,
        customerLng: Double
    ): Double {
        if (restaurant == null || !restaurant.hasCoordinates || (customerLat == 0.0 && customerLng == 0.0)) {
            return DeliveryFeeCalculator.calculate(restaurant, customerLat, customerLng)
        }

        val remoteFee = foodRepository.calculateDeliveryFee(
            restaurantLat = restaurant.latitude,
            restaurantLng = restaurant.longitude,
            customerLat = customerLat,
            customerLng = customerLng
        ).getOrNull()

        return remoteFee ?: DeliveryFeeCalculator.calculate(restaurant, customerLat, customerLng)
    }
}

