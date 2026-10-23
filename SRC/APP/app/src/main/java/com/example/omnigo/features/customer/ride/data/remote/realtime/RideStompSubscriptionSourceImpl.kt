package com.example.omnigo.features.customer.ride.data.remote.realtime

import com.example.omnigo.core.websocket.StompClientManager
import com.example.omnigo.core.websocket.StompCommand
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideStompSubscriptionSourceImpl @Inject constructor(
    private val stompClientManager: StompClientManager,
    private val gson: Gson,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : RideStompSubscriptionSource {

    override fun subscribeToRideOffers(): Flow<BookingResponse> {
        scope.launch { stompClientManager.ensureBookingConnected() }
        return stompClientManager.bookingClient
            .subscribe(RideStompDestinations.DRIVER_RIDE_MATCH)
            .mapNotNull { frame ->
                if (frame.command != StompCommand.MESSAGE || frame.body.isNullOrBlank()) {
                    null
                } else {
                    parsePayloadSafely<BookingResponse>(frame.body)
                }
            }
    }

    override fun subscribeToBookingStatus(): Flow<BookingResponse> {
        scope.launch { stompClientManager.ensureBookingConnected() }
        return stompClientManager.bookingClient
            .subscribe(RideStompDestinations.BOOKING_STATUS)
            .mapNotNull { frame ->
                if (frame.command != StompCommand.MESSAGE || frame.body.isNullOrBlank()) {
                    null
                } else {
                    parsePayloadSafely<BookingResponse>(frame.body)
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
