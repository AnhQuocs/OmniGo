package com.example.omnigo.features.customer.ride.domain.repository

import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult

interface RideBookingRepository {
    suspend fun getBookingDriver(bookingId: Long): GetBookingDriverResult
}
