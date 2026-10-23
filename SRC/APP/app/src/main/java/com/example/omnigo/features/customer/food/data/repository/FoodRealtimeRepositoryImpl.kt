package com.example.omnigo.features.customer.food.data.repository

import com.example.omnigo.features.customer.food.data.mapper.toDomain
import com.example.omnigo.features.customer.food.data.remote.realtime.FoodStompSubscriptionSource
import com.example.omnigo.features.customer.food.domain.model.DriverLocation
import com.example.omnigo.features.customer.food.domain.model.FoodOrderAssignmentEvent
import com.example.omnigo.features.customer.food.domain.model.FoodOrderStatusEvent
import com.example.omnigo.features.customer.food.domain.repository.FoodRealtimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRealtimeRepositoryImpl @Inject constructor(
    private val stompSource: FoodStompSubscriptionSource
) : FoodRealtimeRepository {

    override fun observeFoodOrderAssignments(): Flow<FoodOrderAssignmentEvent> {
        return stompSource.subscribeToFoodOrderAssignments().mapNotNull { it.toDomain() }
    }

    override fun observeFoodOrderStatus(orderId: Long): Flow<FoodOrderStatusEvent> {
        return stompSource.subscribeToFoodOrderStatus(orderId).mapNotNull { it.toDomain() }
    }

    override fun observeDriverLocation(driverId: Long): Flow<DriverLocation> {
        return stompSource.subscribeToDriverLocation(driverId).mapNotNull { it.toDomain() }
    }
}
