package com.example.omnigo.features.customer.ride.domain.repository

import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CreateRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult

interface RideBookingRepository {
    suspend fun createBooking(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): CreateRideBookingResult

    suspend fun cancelBooking(bookingId: Long): CancelRideBookingResult

    suspend fun getBookingDriver(bookingId: Long): GetBookingDriverResult
}
