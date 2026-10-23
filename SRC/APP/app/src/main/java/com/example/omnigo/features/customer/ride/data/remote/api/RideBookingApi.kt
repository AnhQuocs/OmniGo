package com.example.omnigo.features.customer.ride.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingDriverResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface RideBookingApi {

    @GET(ApiEndpoints.BOOKING_DRIVER)
    suspend fun getBookingDriver(
        @Path("bookingId") bookingId: Long
    ): ApiResponse<BookingDriverResponse>
}
