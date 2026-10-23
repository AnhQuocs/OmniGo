package com.example.omnigo.features.customer.ride.domain.repository

import com.example.omnigo.features.customer.ride.domain.model.AcceptRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CreateRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.RejectRideBookingResult
import com.example.omnigo.features.driver.domain.model.RideOfferEvent
import kotlinx.coroutines.flow.Flow

interface RideBookingRepository {
    suspend fun createBooking(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): CreateRideBookingResult

    suspend fun cancelBooking(bookingId: Long): CancelRideBookingResult

    suspend fun getBookingDriver(bookingId: Long): GetBookingDriverResult

    suspend fun acceptBooking(bookingId: Long): AcceptRideBookingResult

    suspend fun rejectBooking(bookingId: Long): RejectRideBookingResult

    fun observeRideOffers(): Flow<RideOfferEvent>
}
