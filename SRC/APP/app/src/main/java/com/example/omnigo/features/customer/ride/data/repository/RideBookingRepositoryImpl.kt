package com.example.omnigo.features.customer.ride.data.repository

import com.example.omnigo.core.network.toBackendMessage
import com.example.omnigo.features.customer.ride.data.mapper.toDomain
import com.example.omnigo.features.customer.ride.data.mapper.toRideOfferEvent
import com.example.omnigo.features.customer.ride.data.mapper.toRideStatusEvent
import com.example.omnigo.features.customer.ride.data.remote.api.RideBookingApi
import com.example.omnigo.features.customer.ride.data.remote.dto.BookingRequest
import com.example.omnigo.features.customer.ride.data.remote.realtime.RideStompSubscriptionSource
import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.AcceptRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.ArriveRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CompleteRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CreateRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.model.GetDriverBookingsResult
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.domain.model.NearbyDriver
import com.example.omnigo.features.customer.ride.domain.model.RejectRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent
import com.example.omnigo.features.customer.ride.domain.model.StartRideBookingResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.domain.model.RideOfferEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RideBookingRepositoryImpl @Inject constructor(
    private val rideBookingApi: RideBookingApi,
    private val rideStompSubscriptionSource: RideStompSubscriptionSource
) : RideBookingRepository {

    internal var clock: () -> Long = { System.currentTimeMillis() }
    private var cachedDailyReport: DriverDailyReport? = null
    private var lastDailyReportFetchMs: Long = 0L

    companion object {
        const val DAILY_REPORT_CACHE_TTL_MS = 5 * 60 * 1000L
    }

    override suspend fun createBooking(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
        vehicleType: String?,
        pickupAddress: String?,
        dropoffAddress: String?
    ): CreateRideBookingResult {
        return try {
            val request = BookingRequest(
                startLongitude = startLongitude,
                startLatitude = startLatitude,
                endLongitude = endLongitude,
                endLatitude = endLatitude,
                vehicleType = vehicleType,
                pickupAddress = pickupAddress,
                dropoffAddress = dropoffAddress
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
                message = e.toBackendMessage()
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
                message = e.toBackendMessage()
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

    override suspend fun acceptBooking(bookingId: Long): AcceptRideBookingResult {
        return try {
            val response = rideBookingApi.acceptBooking(bookingId)
            AcceptRideBookingResult.Success(response.toDomain())
        } catch (e: IOException) {
            AcceptRideBookingResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: HttpException) {
            val error = if (e.code() == 404) RideError.BOOKING_NOT_FOUND else RideError.SERVER_ERROR
            AcceptRideBookingResult.Error(
                error = error,
                message = e.message()
            )
        } catch (e: Exception) {
            AcceptRideBookingResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }

    override suspend fun rejectBooking(bookingId: Long): RejectRideBookingResult {
        return try {
            val response = rideBookingApi.driverCancelBooking(bookingId)
            RejectRideBookingResult.Success(response.toDomain())
        } catch (e: IOException) {
            RejectRideBookingResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: HttpException) {
            val error = if (e.code() == 404) RideError.BOOKING_NOT_FOUND else RideError.SERVER_ERROR
            RejectRideBookingResult.Error(
                error = error,
                message = e.message()
            )
        } catch (e: Exception) {
            RejectRideBookingResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }

    override suspend fun arriveBooking(bookingId: Long): ArriveRideBookingResult {
        return try {
            val response = rideBookingApi.arriveBooking(bookingId)
            ArriveRideBookingResult.Success(response.toDomain())
        } catch (e: IOException) {
            ArriveRideBookingResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: HttpException) {
            val error = if (e.code() == 404) RideError.BOOKING_NOT_FOUND else RideError.SERVER_ERROR
            ArriveRideBookingResult.Error(
                error = error,
                message = e.message()
            )
        } catch (e: Exception) {
            ArriveRideBookingResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }

    override suspend fun startBooking(bookingId: Long): StartRideBookingResult {
        return try {
            val response = rideBookingApi.startBooking(bookingId)
            StartRideBookingResult.Success(response.toDomain())
        } catch (e: IOException) {
            StartRideBookingResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: HttpException) {
            val error = if (e.code() == 404) RideError.BOOKING_NOT_FOUND else RideError.SERVER_ERROR
            StartRideBookingResult.Error(
                error = error,
                message = e.message()
            )
        } catch (e: Exception) {
            StartRideBookingResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }

    override suspend fun completeBooking(bookingId: Long, paymentMethod: String?): CompleteRideBookingResult {
        return try {
            val response = rideBookingApi.completeBooking(bookingId, paymentMethod)
            invalidateDailyReportCache()
            CompleteRideBookingResult.Success(response.toDomain())
        } catch (e: IOException) {
            CompleteRideBookingResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: HttpException) {
            val error = if (e.code() == 404) RideError.BOOKING_NOT_FOUND else RideError.SERVER_ERROR
            CompleteRideBookingResult.Error(
                error = error,
                message = e.message()
            )
        } catch (e: Exception) {
            CompleteRideBookingResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }

    override fun observeRideOffers(): Flow<RideOfferEvent> {
        return rideStompSubscriptionSource.subscribeToRideOffers()
            .mapNotNull { it.toRideOfferEvent() }
    }

    override fun observeBookingStatus(bookingId: Long): Flow<RideStatusEvent> {
        return rideStompSubscriptionSource.subscribeToBookingStatus()
            .mapNotNull { it.toRideStatusEvent() }
            .filter { it.bookingId == bookingId }
    }

    override fun observeDriverLiveLocation(driverId: Long): Flow<DriverLiveLocation> {
        return rideStompSubscriptionSource.subscribeToDriverLocation(driverId)
            .mapNotNull { it.toDomain() }
    }

    override suspend fun getCustomerBookings(): GetCustomerBookingsResult {
        return try {
            val response = rideBookingApi.getCustomerBookings()
            if (response.success && response.data != null) {
                val bookings = response.data.map { it.toDomain() }
                GetCustomerBookingsResult.Success(bookings)
            } else {
                GetCustomerBookingsResult.Error(
                    error = RideError.UNKNOWN_ERROR,
                    message = response.message
                )
            }
        } catch (e: HttpException) {
            GetCustomerBookingsResult.Error(
                error = RideError.SERVER_ERROR,
                message = e.message()
            )
        } catch (e: IOException) {
            GetCustomerBookingsResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: Exception) {
            GetCustomerBookingsResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }

    override suspend fun getDriverBookings(): GetDriverBookingsResult {
        return try {
            val response = rideBookingApi.getDriverBookings()
            if (response.success && response.data != null) {
                val bookings = response.data.map { it.toDomain() }
                GetDriverBookingsResult.Success(bookings)
            } else {
                GetDriverBookingsResult.Error(
                    error = RideError.UNKNOWN_ERROR,
                    message = response.message
                )
            }
        } catch (e: HttpException) {
            GetDriverBookingsResult.Error(
                error = RideError.SERVER_ERROR,
                message = e.message()
            )
        } catch (e: IOException) {
            GetDriverBookingsResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: Exception) {
            GetDriverBookingsResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }

    override fun invalidateDailyReportCache() {
        cachedDailyReport = null
        lastDailyReportFetchMs = 0L
    }

    override suspend fun getDriverDailyReport(
        forceRefresh: Boolean,
        period: String?,
        date: String?
    ): GetDriverDailyReportResult {
        val isTodayReport = (period == null || period == "DAY") && date == null
        val cached = cachedDailyReport
        if (isTodayReport && !forceRefresh && cached != null && clock() - lastDailyReportFetchMs < DAILY_REPORT_CACHE_TTL_MS) {
            return GetDriverDailyReportResult.Success(cached)
        }
        return try {
            val response = rideBookingApi.getDriverDailyReport(period = period, date = date)
            if (response.success && response.data != null) {
                val report = response.data.toDomain()
                if (isTodayReport) {
                    cachedDailyReport = report
                    lastDailyReportFetchMs = clock()
                }
                GetDriverDailyReportResult.Success(report)
            } else {
                GetDriverDailyReportResult.Error(
                    error = RideError.UNKNOWN_ERROR,
                    message = response.message
                )
            }
        } catch (e: HttpException) {
            GetDriverDailyReportResult.Error(
                error = RideError.SERVER_ERROR,
                message = e.message()
            )
        } catch (e: IOException) {
            GetDriverDailyReportResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: Exception) {
            GetDriverDailyReportResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }

    override suspend fun getNearbyDrivers(
        latitude: Double,
        longitude: Double,
        radius: Double
    ): List<NearbyDriver> {
        return try {
            val response = rideBookingApi.getNearbyDrivers(
                longitude = longitude,
                latitude = latitude,
                radius = radius
            )
            response.toDomain()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
