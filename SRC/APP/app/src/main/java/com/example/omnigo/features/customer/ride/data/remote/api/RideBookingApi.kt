package com.example.omnigo.features.customer.ride.data.remote.api

import com.example.omnigo.core.network.ApiEndpoints
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingDriverResponse
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingRequest
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingResponse
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingReviewRequestDto
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingReviewResponseDto
import com.example.omnigo.features.customer.ride.data.remote.dto.DriverBookingReviewsEnvelopeDto
import com.example.omnigo.features.driver.data.remote.dto.DriverDailyReportDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RideBookingApi {

    @POST(ApiEndpoints.BOOKINGS)
    suspend fun createBooking(
        @Header("Idempotency-Key") idempotencyKey: String? = null,
        @Body request: BookingRequest
    ): BookingResponse

    @GET(ApiEndpoints.BOOKING_DETAIL)
    suspend fun getBookingDetail(
        @Path("bookingId") bookingId: Long
    ): Response<ApiResponse<BookingResponse>>

    @POST(ApiEndpoints.BOOKING_REVIEWS)
    suspend fun submitBookingReview(
        @Path("bookingId") bookingId: Long,
        @Body request: BookingReviewRequestDto
    ): Response<ApiResponse<BookingReviewResponseDto>>

    @GET(ApiEndpoints.BOOKING_REVIEWS)
    suspend fun getBookingReview(
        @Path("bookingId") bookingId: Long
    ): Response<ApiResponse<BookingReviewResponseDto>>

    @GET(ApiEndpoints.BOOKING_DRIVER_REVIEWS)
    suspend fun getDriverBookingReviews(
        @Path("driverId") driverId: Long
    ): Response<ApiResponse<DriverBookingReviewsEnvelopeDto>>

    @PUT(ApiEndpoints.BOOKING_CANCEL)
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: Long
    ): BookingResponse

    @GET(ApiEndpoints.BOOKING_DRIVER)
    suspend fun getBookingDriver(
        @Path("bookingId") bookingId: Long
    ): ApiResponse<BookingDriverResponse>

    @PUT(ApiEndpoints.BOOKING_ACCEPT)
    suspend fun acceptBooking(
        @Path("bookingId") bookingId: Long
    ): BookingResponse

    @PUT(ApiEndpoints.BOOKING_DRIVER_CANCEL)
    suspend fun driverCancelBooking(
        @Path("bookingId") bookingId: Long
    ): BookingResponse

    @PUT(ApiEndpoints.BOOKING_ARRIVED)
    suspend fun arriveBooking(
        @Path("bookingId") bookingId: Long
    ): BookingResponse

    @PUT(ApiEndpoints.BOOKING_START)
    suspend fun startBooking(
        @Path("bookingId") bookingId: Long
    ): BookingResponse

    @PUT(ApiEndpoints.BOOKING_COMPLETE)
    suspend fun completeBooking(
        @Path("bookingId") bookingId: Long,
        @Query("paymentMethod") paymentMethod: String? = "CASH"
    ): BookingResponse

    @GET(ApiEndpoints.BOOKINGS_CUSTOMER)
    suspend fun getCustomerBookings(): ApiResponse<List<BookingResponse>>

    @GET(ApiEndpoints.BOOKINGS_DRIVER)
    suspend fun getDriverBookings(): ApiResponse<List<BookingResponse>>

    @GET(ApiEndpoints.BOOKINGS_DRIVER_DAILY_REPORT)
    suspend fun getDriverDailyReport(
        @Query("period") period: String? = null,
        @Query("date") date: String? = null
    ): ApiResponse<DriverDailyReportDto>

    @GET(ApiEndpoints.LOCATIONS_NEARBY)
    suspend fun getNearbyDrivers(
        @Query("longitude") longitude: Double,
        @Query("latitude") latitude: Double,
        @Query("radius") radius: Double = 5.0
    ): List<com.example.omnigo.features.customer.ride.data.remote.dto.DriverLocationResponse>
}
