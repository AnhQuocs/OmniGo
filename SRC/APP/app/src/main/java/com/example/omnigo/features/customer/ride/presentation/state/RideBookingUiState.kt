package com.example.omnigo.features.customer.ride.presentation.state

import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate

data class LocationPoint(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false
) {
    companion object {
        fun fromUserLocation(userLocation: UserLocation): LocationPoint {
            val title = "Vị trí hiện tại"
            val desc = userLocation.fullAddress.ifBlank {
                userLocation.addressName.ifBlank { "Lat: ${userLocation.latitude}, Lng: ${userLocation.longitude}" }
            }
            return LocationPoint(
                name = title,
                address = desc,
                latitude = userLocation.latitude,
                longitude = userLocation.longitude,
                isCurrentLocation = true
            )
        }

        fun fromSavedAddress(saved: SavedAddress): LocationPoint {
            return LocationPoint(
                name = saved.addressName,
                address = saved.fullAddress,
                latitude = saved.latitude,
                longitude = saved.longitude,
                isCurrentLocation = false
            )
        }
    }
}

enum class LocationTarget {
    PICKUP,
    DROPOFF
}

data class RideBookingUiState(
    val pickupLocation: LocationPoint? = null,
    val dropoffLocation: LocationPoint? = null,
    val savedAddresses: List<SavedAddress> = emptyList(),
    val isLocatingCurrentPosition: Boolean = false,
    val isCalculatingPrice: Boolean = false,
    val pricingEstimate: PricingEstimate? = null,
    val pricingError: com.example.omnigo.utils.UiText? = null,
    val isCreatingBooking: Boolean = false,
    val currentBooking: Booking? = null,
    val isSearchingDriver: Boolean = false,
    val searchCountdownSeconds: Int = 60,
    val isCancellingBooking: Boolean = false,
    val errorMessage: com.example.omnigo.utils.UiText? = null,
    val infoMessage: com.example.omnigo.utils.UiText? = null,
    val assignedDriver: BookingDriverProfile? = null,
    val isDriverProfileLoading: Boolean = false,
    val tripStatusMessage: String? = null,
    val driverLiveLocation: DriverLiveLocation? = null,
    val liveDistanceKm: Double? = null,
    val liveEtaMinutes: Int? = null,
    val showSearchTimeoutDialog: Boolean = false,
    val selectedVehicleType: com.example.omnigo.features.auth.domain.model.VehicleType = com.example.omnigo.features.auth.domain.model.VehicleType.BIKE,
    val vehicleEstimates: Map<com.example.omnigo.features.auth.domain.model.VehicleType, PricingEstimate> = emptyMap(),
    val nearbyDrivers: List<com.example.omnigo.features.customer.ride.domain.model.NearbyDriver> = emptyList(),
    val isMapPinPicking: Boolean = false,
    val pinPickingTarget: LocationTarget? = null,
    val pinPickedLocation: LocationPoint? = null,
    val isPickupPinConfirmed: Boolean = false,
    val pendingDropoffPinLocation: LocationPoint? = null,
    val isGeocodingPin: Boolean = false,
    val isSearchingPlaces: Boolean = false,
    val searchTarget: LocationTarget = LocationTarget.DROPOFF,
    val searchQuery: String = "",
    val searchResults: List<LocationPoint> = emptyList(),
    val isSearchingPlacesLoading: Boolean = false,
    val routeCoordinates: List<Pair<Double, Double>> = emptyList()
) {
    val isDualLocationReady: Boolean
        get() = pickupLocation != null && dropoffLocation != null

    val isTripActive: Boolean
        get() = currentBooking != null && (
            currentBooking.status == BookingStatus.ACCEPTED ||
            currentBooking.status == BookingStatus.ARRIVED ||
            currentBooking.status == BookingStatus.IN_PROGRESS
        )
}
