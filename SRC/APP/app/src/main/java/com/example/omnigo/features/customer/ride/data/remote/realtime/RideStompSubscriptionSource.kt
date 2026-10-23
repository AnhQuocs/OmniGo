package com.example.omnigo.features.customer.ride.data.remote.realtime

import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import kotlinx.coroutines.flow.Flow

interface RideStompSubscriptionSource {
    fun subscribeToRideOffers(): Flow<BookingResponse>
    fun subscribeToBookingStatus(): Flow<BookingResponse>
}
