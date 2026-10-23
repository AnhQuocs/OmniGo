package com.example.omnigo.features.customer.ride.presentation.state

import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate

data class LocationPoint(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        fun fromUserLocation(userLocation: UserLocation): LocationPoint {
            val title = userLocation.addressName.ifBlank { "Current Location" }
            val desc = userLocation.fullAddress.ifBlank { "Lat: ${userLocation.latitude}, Lng: ${userLocation.longitude}" }
            return LocationPoint(
                name = title,
                address = desc,
                latitude = userLocation.latitude,
                longitude = userLocation.longitude
            )
        }

        fun fromSavedAddress(saved: SavedAddress): LocationPoint {
            return LocationPoint(
                name = saved.addressName,
                address = saved.fullAddress,
                latitude = saved.latitude,
                longitude = saved.longitude
            )
        }
    }
}

data class RideBookingUiState(
    val pickupLocation: LocationPoint? = null,
    val dropoffLocation: LocationPoint? = null,
    val savedAddresses: List<SavedAddress> = emptyList(),
    val isLocatingCurrentPosition: Boolean = false,
    val isCalculatingPrice: Boolean = false,
    val pricingEstimate: PricingEstimate? = null,
    val pricingError: String? = null,
    val isCreatingBooking: Boolean = false,
    val currentBooking: Booking? = null,
    val isSearchingDriver: Boolean = false,
    val searchCountdownSeconds: Int = 60,
    val isCancellingBooking: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)
