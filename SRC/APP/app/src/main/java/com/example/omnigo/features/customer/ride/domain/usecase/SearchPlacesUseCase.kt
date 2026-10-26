package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val locationTracker: LocationTracker
) {
    suspend operator fun invoke(query: String): List<LocationPoint> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()

        val results = locationTracker.searchPlaces(trimmed)
        return results.map { userLocation ->
            LocationPoint(
                name = userLocation.addressName,
                address = userLocation.fullAddress,
                latitude = userLocation.latitude,
                longitude = userLocation.longitude
            )
        }
    }
}
