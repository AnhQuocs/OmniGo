package com.example.omnigo.features.customer.ride.domain.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object BearingCalculator {

    /**
     * Calculates the initial bearing (forward azimuth) from point (lat1, lon1) to point (lat2, lon2)
     * in degrees within range [0, 360).
     */
    fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)
        val degrees = Math.toDegrees(theta)

        return normalizeBearing(degrees.toFloat())
    }

    /**
     * Normalizes any angle in degrees into the standard [0, 360) degree range.
     */
    fun normalizeBearing(degrees: Float): Float {
        val normalized = degrees % 360f
        return if (normalized < 0f) normalized + 360f else normalized
    }
}
