package com.example.omnigo.features.customer.food.domain.repository

import com.example.omnigo.features.customer.food.domain.model.FoodOrderTrackingState
import kotlinx.coroutines.flow.StateFlow

interface ActiveFoodOrderTrackingRepository {
    val activeTrackingState: StateFlow<FoodOrderTrackingState?>

    suspend fun activate(orderId: Long)

    suspend fun clear()
}
