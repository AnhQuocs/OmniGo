package com.example.omnigo.features.customer.ride.data.repository

import com.example.omnigo.features.customer.ride.data.mapper.toDomain
import com.example.omnigo.features.customer.ride.data.remote.api.RideBookingApi
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingRequest
import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CreateRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RideBookingRepositoryImpl @Inject constructor(
    private val rideBookingApi: RideBookingApi
) : RideBookingRepository {

    override suspend fun createBooking(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): CreateRideBookingResult {
        return try {
            val request = BookingRequest(
                startLongitude = startLongitude,
                startLatitude = startLatitude,
                endLongitude = endLongitude,
                endLatitude = endLatitude
            )
            val response = rideBookingApi.createBooking(request)
            CreateRideBookingResult.Success(response.toDomain())
        } catch (e: IOException) {
            CreateRideBookingResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: HttpException) {
            CreateRideBookingResult.Error(
                error = RideError.SERVER_ERROR,
                message = e.message()
            )
        } catch (e: Exception) {
            CreateRideBookingResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }

    override suspend fun cancelBooking(bookingId: Long): CancelRideBookingResult {
        return try {
            val response = rideBookingApi.cancelBooking(bookingId)
            CancelRideBookingResult.Success(response.toDomain())
        } catch (e: IOException) {
            CancelRideBookingResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: HttpException) {
            val error = if (e.code() == 404) RideError.BOOKING_NOT_FOUND else RideError.SERVER_ERROR
            CancelRideBookingResult.Error(
                error = error,
                message = e.message()
            )
        } catch (e: Exception) {
            CancelRideBookingResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }

    override suspend fun getBookingDriver(bookingId: Long): GetBookingDriverResult {
        return try {
            val response = rideBookingApi.getBookingDriver(bookingId)
            val driverData = response.data
            if (response.success && driverData != null) {
                GetBookingDriverResult.Success(driverData.toDomain())
            } else {
                GetBookingDriverResult.Error(
                    error = RideError.DRIVER_NOT_FOUND,
                    message = response.message ?: "Driver not found for booking $bookingId"
                )
            }
        } catch (e: IOException) {
            GetBookingDriverResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: HttpException) {
            val error = if (e.code() == 404) RideError.DRIVER_NOT_FOUND else RideError.SERVER_ERROR
            GetBookingDriverResult.Error(
                error = error,
                message = e.message()
            )
        } catch (e: Exception) {
            GetBookingDriverResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }
}
