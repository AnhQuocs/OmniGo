package com.example.omnigo.features.customer.food.data.remote.realtime

import com.example.omnigo.features.customer.food.data.remote.dto.DriverLocationEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderAssignmentEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderStatusEventDto
import kotlinx.coroutines.flow.Flow

interface FoodStompSubscriptionSource {
    fun subscribeToFoodOrderAssignments(): Flow<FoodOrderAssignmentEventDto>

    fun subscribeToFoodOrderStatus(orderId: Long): Flow<FoodOrderStatusEventDto>

    fun subscribeToDriverLocation(driverId: Long): Flow<DriverLocationEventDto>
}
