package com.example.omnigo.features.driver.home.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer
import com.example.omnigo.features.driver.domain.model.RideOfferEvent
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder

data class DriverHomeUiState(
    val isOnline: Boolean = false,
    val approvalStatus: DriverApprovalStatus = DriverApprovalStatus.APPROVED,
    val profile: DriverAccountProfile? = null,
    val dailyReport: DriverDailyReport? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val activeRideBooking: Booking? = null,
    val activeFoodOrder: DriverDeliveryOrder? = null,
    val isLoading: Boolean = false,
    val isStatusUpdating: Boolean = false,
    val errorMessage: String? = null,
    val showApprovalWarningDialog: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val isGpsEnabled: Boolean = false,
    val currentFoodOffer: DriverFoodOffer? = null,
    val offerCountdownSeconds: Int = DriverFoodOffer.DEFAULT_TIMEOUT_SECONDS,
    val offerCountdownProgress: Float = 1f,
    val isOfferAccepting: Boolean = false,
    val isOfferRejecting: Boolean = false,
    val offerActionMessage: String? = null,
    val acceptedOrderId: Long? = null,
    val acceptedOfferRestaurantLat: Double? = null,
    val acceptedOfferRestaurantLng: Double? = null,
    val currentRideOffer: RideOfferEvent? = null,
    val rideOfferCountdownSeconds: Int = RideOfferEvent.DEFAULT_TIMEOUT_SECONDS,
    val rideOfferCountdownProgress: Float = 1f,
    val isRideOfferAccepting: Boolean = false,
    val isRideOfferRejecting: Boolean = false,
    val acceptedRideBookingId: Long? = null
)
