package com.example.omnigo.features.customer.food.data.remote.realtime

import com.example.omnigo.core.websocket.StompClientManager
import com.example.omnigo.core.websocket.StompCommand
import com.example.omnigo.features.customer.food.data.remote.dto.DriverLocationEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOfferEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderAssignmentEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderStatusEventDto
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodStompSubscriptionSourceImpl @Inject constructor(
    private val stompClientManager: StompClientManager,
    private val gson: Gson,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : FoodStompSubscriptionSource {

    override fun subscribeToFoodOrderAssignments(): Flow<FoodOrderAssignmentEventDto> {
        scope.launch { stompClientManager.ensureBookingConnected() }
        return stompClientManager.bookingClient
            .subscribe(FoodStompDestinations.FOOD_ORDER_ASSIGNMENT)
            .mapNotNull { frame ->
                if (frame.command != StompCommand.MESSAGE || frame.body.isNullOrBlank()) {
                    null
                } else {
                    parsePayloadSafely<FoodOrderAssignmentEventDto>(frame.body)
                }
            }
    }

    override fun subscribeToFoodOrderStatus(orderId: Long): Flow<FoodOrderStatusEventDto> {
        scope.launch { stompClientManager.ensureBookingConnected() }
        val destination = FoodStompDestinations.FOOD_ORDER_STATUS
        return stompClientManager.bookingClient
            .subscribe(destination)
            .mapNotNull { frame ->
                if (frame.command != StompCommand.MESSAGE || frame.body.isNullOrBlank()) {
                    null
                } else {
                    parsePayloadSafely<FoodOrderStatusEventDto>(frame.body)
                }
            }
            .filter { event ->
                orderId <= 0L || event.orderId == orderId
            }
    }

    override fun subscribeToDriverLocation(driverId: Long): Flow<DriverLocationEventDto> {
        scope.launch { stompClientManager.ensureLocationConnected() }
        val destination = FoodStompDestinations.driverLocation(driverId)
        return stompClientManager.locationClient
            .subscribe(destination)
            .mapNotNull { frame ->
                if (frame.command != StompCommand.MESSAGE || frame.body.isNullOrBlank()) {
                    null
                } else {
                    parsePayloadSafely<DriverLocationEventDto>(frame.body)
                }
            }
    }

    override fun subscribeToFoodOffers(): Flow<FoodOfferEventDto> {
        scope.launch { stompClientManager.ensureBookingConnected() }
        return stompClientManager.bookingClient
            .subscribe(FoodStompDestinations.FOOD_OFFERS)
            .mapNotNull { frame ->
                if (frame.command != StompCommand.MESSAGE || frame.body.isNullOrBlank()) {
                    null
                } else {
                    parsePayloadSafely<FoodOfferEventDto>(frame.body)
                }
            }
    }

    private inline fun <reified T> parsePayloadSafely(json: String): T? {
        return try {
            gson.fromJson(json, T::class.java)
        } catch (_: Exception) {
            null
        }
    }
}
