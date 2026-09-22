package com.example.omnigo.features.customer.home.domain.usecase

import com.example.omnigo.core.location.tracker.LocationTracker
import javax.inject.Inject

class GeocodeAddressUseCase @Inject constructor(
    private val locationTracker: LocationTracker
) {
    suspend operator fun invoke(fullAddress: String): Pair<Double, Double>? {
        return locationTracker.getCoordinatesFromAddress(fullAddress)
    }
}
