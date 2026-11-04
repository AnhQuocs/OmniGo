package com.example.omnigo.features.customer.food.domain.util

import com.example.omnigo.features.customer.food.domain.model.Restaurant
import kotlin.math.ceil

/**
 * Tính phí giao hàng đồ ăn theo biểu phí luỹ tiến chính thức (Pricing API #57):
 * - Cự ly < 50m (0.05km): Tự động bypass 0km -> 0 ₫ (miễn phí)
 * - 0–2km: 15.000 ₫
 * - 2–5km: +5.000 ₫/km
 * - Trên 5km: +4.000 ₫/km
 * - Hệ số đường bộ khi tính chim bay (Haversine fallback): 1.35
 */
object DeliveryFeeCalculator {
    const val BASE_FEE = 15000.0
    const val TIER_1_DISTANCE_KM = 2.0
    const val TIER_2_DISTANCE_KM = 5.0
    const val RATE_TIER_2_PER_KM = 5000.0
    const val RATE_TIER_3_PER_KM = 4000.0
    const val MIN_DISTANCE_BYPASS_KM = 0.05
    const val HAVERSINE_ROAD_FACTOR = 1.35

    fun calculate(restaurant: Restaurant?, dropOffLat: Double, dropOffLng: Double): Double {
        if (restaurant == null || !restaurant.hasCoordinates || (dropOffLat == 0.0 && dropOffLng == 0.0)) {
            return 0.0
        }

        val airDistanceKm = GeoDistance.haversineKm(
            dropOffLat,
            dropOffLng,
            restaurant.latitude,
            restaurant.longitude
        )

        if (airDistanceKm < MIN_DISTANCE_BYPASS_KM) {
            return 0.0
        }

        val roadDistanceKm = airDistanceKm * HAVERSINE_ROAD_FACTOR

        return when {
            roadDistanceKm <= TIER_1_DISTANCE_KM -> BASE_FEE
            roadDistanceKm <= TIER_2_DISTANCE_KM -> {
                val extraKm = roadDistanceKm - TIER_1_DISTANCE_KM
                BASE_FEE + ceil(extraKm) * RATE_TIER_2_PER_KM
            }
            else -> {
                val tier2Span = TIER_2_DISTANCE_KM - TIER_1_DISTANCE_KM // 3.0 km
                val tier2Fee = tier2Span * RATE_TIER_2_PER_KM // 15,000
                val extraKm = roadDistanceKm - TIER_2_DISTANCE_KM
                BASE_FEE + tier2Fee + ceil(extraKm) * RATE_TIER_3_PER_KM
            }
        }
    }
}
