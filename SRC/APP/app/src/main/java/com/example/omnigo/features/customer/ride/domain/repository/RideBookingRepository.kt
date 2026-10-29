package com.example.omnigo.features.customer.ride.domain.repository

import com.example.omnigo.features.customer.ride.domain.model.AcceptRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.ArriveRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CompleteRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.CreateRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.customer.ride.domain.model.RejectRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent
import com.example.omnigo.features.customer.ride.domain.model.StartRideBookingResult
import com.example.omnigo.features.driver.domain.model.RideOfferEvent
import kotlinx.coroutines.flow.Flow

interface RideBookingRepository {
    suspend fun createBooking(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
        vehicleType: String? = null,
        pickupAddress: String? = null,
        dropoffAddress: String? = null
    ): CreateRideBookingResult

    suspend fun cancelBooking(bookingId: Long): CancelRideBookingResult

    suspend fun getBookingDriver(bookingId: Long): GetBookingDriverResult

    suspend fun acceptBooking(bookingId: Long): AcceptRideBookingResult

    suspend fun rejectBooking(bookingId: Long): RejectRideBookingResult

    suspend fun arriveBooking(bookingId: Long): ArriveRideBookingResult

    suspend fun startBooking(bookingId: Long): StartRideBookingResult

    suspend fun completeBooking(bookingId: Long, paymentMethod: String? = "CASH"): CompleteRideBookingResult

    fun observeRideOffers(): Flow<RideOfferEvent>

    fun observeBookingStatus(bookingId: Long): Flow<RideStatusEvent>

    fun observeDriverLiveLocation(driverId: Long): Flow<DriverLiveLocation>

    suspend fun getCustomerBookings(): GetCustomerBookingsResult

    suspend fun getDriverDailyReport(forceRefresh: Boolean = false): GetDriverDailyReportResult

    fun invalidateDailyReportCache()

    suspend fun getNearbyDrivers(
        latitude: Double,
        longitude: Double,
        radius: Double = 5.0
    ): List<com.example.omnigo.features.customer.ride.domain.model.NearbyDriver>
}
