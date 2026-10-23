package com.example.omnigo.features.customer.ride.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingDriverResponse
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingRequest
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RideBookingApi {

    @POST(ApiEndpoints.BOOKINGS)
    suspend fun createBooking(
        @Body request: BookingRequest
    ): BookingResponse

    @PUT(ApiEndpoints.BOOKING_CANCEL)
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: Long
    ): BookingResponse

    @GET(ApiEndpoints.BOOKING_DRIVER)
    suspend fun getBookingDriver(
        @Path("bookingId") bookingId: Long
    ): ApiResponse<BookingDriverResponse>
}
