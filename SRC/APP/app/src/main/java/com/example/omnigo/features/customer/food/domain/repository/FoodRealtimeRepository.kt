package com.example.omnigo.features.customer.food.domain.repository

import com.example.omnigo.features.customer.food.domain.model.DriverLocation
import com.example.omnigo.features.customer.food.domain.model.FoodOrderAssignmentEvent
import com.example.omnigo.features.customer.food.domain.model.FoodOrderStatusEvent
import kotlinx.coroutines.flow.Flow

interface FoodRealtimeRepository {
    fun observeFoodOrderAssignments(): Flow<FoodOrderAssignmentEvent>

    fun observeFoodOrderStatus(orderId: Long): Flow<FoodOrderStatusEvent>

    fun observeDriverLocation(driverId: Long): Flow<DriverLocation>
}
