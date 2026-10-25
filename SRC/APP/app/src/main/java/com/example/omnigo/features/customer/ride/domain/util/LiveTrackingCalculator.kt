package com.example.omnigo.features.customer.ride.domain.util

import com.example.omnigo.features.driver.domain.util.RideGeofenceCalculator
import kotlin.math.ceil
import kotlin.math.max

object LiveTrackingCalculator {

    const val DEFAULT_FALLBACK_SPEED_KMH = 25.0f

    /**
     * Calculates remaining great-circle distance in kilometers.
     */
    fun calculateRemainingDistanceKm(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double
    ): Double {
        val distanceMeters = RideGeofenceCalculator.calculateDistanceMeters(
            lat1 = fromLat,
            lon1 = fromLng,
            lat2 = toLat,
            lon2 = toLng
        )
        return distanceMeters / 1000.0
    }

    /**
     * Calculates dynamic estimated arrival time in minutes.
     * When stationary or speed is missing, uses default fallback urban speed.
     */
    fun calculateEtaMinutes(
        distanceKm: Double,
        speedKmh: Float?
    ): Int {
        if (distanceKm <= 0.05) return 0
        val effectiveSpeed = if (speedKmh != null && speedKmh > 3.0f) {
            speedKmh
        } else {
            DEFAULT_FALLBACK_SPEED_KMH
        }
        val hours = distanceKm / effectiveSpeed
        val minutes = ceil(hours * 60.0).toInt()
        return max(1, minutes)
    }
}
