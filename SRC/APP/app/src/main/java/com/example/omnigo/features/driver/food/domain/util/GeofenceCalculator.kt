package com.example.omnigo.features.driver.food.domain.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeofenceCalculator {

    const val DEFAULT_GEOFENCE_RADIUS_METERS = 50.0
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates great-circle distance between two points on the earth in meters using Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Checks if distance between driver and target coordinate is within the specified radius threshold.
     */
    fun isWithinRadius(
        lat1: Double?,
        lon1: Double?,
        lat2: Double?,
        lon2: Double?,
        radiusMeters: Double = DEFAULT_GEOFENCE_RADIUS_METERS
    ): Boolean {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return false
        }
        val distance = calculateDistanceMeters(lat1, lon1, lat2, lon2)
        return distance <= radiusMeters
    }
}
