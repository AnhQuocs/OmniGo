package com.example.omnigo.features.customer.food.domain.util

import java.util.Locale

/**
 * Đại diện cho kết quả ước tính thời gian giao hàng và khoảng cách.
 */
data class DeliveryEtaEstimate(
    val minMinutes: Int,
    val maxMinutes: Int,
    val distanceKm: Double? = null
) {
    val durationText: String get() = "$minMinutes - $maxMinutes phút"
}

object RestaurantDeliveryEtaHelper {

    private const val DEFAULT_PREP_MINUTES = 12
    private const val URBAN_ROUTING_FACTOR = 1.35 // Hệ số uốn lượn đường phố đô thị
    private const val BIKE_SPEED_KM_PER_HOUR = 22.0 // Tốc độ di chuyển xe máy trung bình trong nội đô (km/h)

    /**
     * Ước tính khoảng thời gian giao hàng (phút) và khoảng cách (km) giữa nhà hàng và người dùng.
     * Trả về khoảng mặc định 20 - 30 phút nếu thiếu tọa độ hợp lệ.
     */
    fun estimate(
        restaurantLat: Double?,
        restaurantLon: Double?,
        userLat: Double?,
        userLon: Double?
    ): DeliveryEtaEstimate {
        if (!hasValidCoordinates(restaurantLat, restaurantLon) || !hasValidCoordinates(userLat, userLon)) {
            return DeliveryEtaEstimate(minMinutes = 20, maxMinutes = 30, distanceKm = null)
        }

        val straightKm = GeoDistance.haversineKm(
            lat1 = userLat!!,
            lon1 = userLon!!,
            lat2 = restaurantLat!!,
            lon2 = restaurantLon!!
        )

        val roadKm = straightKm * URBAN_ROUTING_FACTOR
        val transitMinutes = (roadKm / BIKE_SPEED_KM_PER_HOUR * 60).toInt()
        val totalMinutes = DEFAULT_PREP_MINUTES + transitMinutes

        val minMinutes = ((totalMinutes / 5) * 5).coerceAtLeast(15)
        val maxMinutes = minMinutes + 10

        return DeliveryEtaEstimate(
            minMinutes = minMinutes,
            maxMinutes = maxMinutes,
            distanceKm = straightKm
        )
    }

    /**
     * Định dạng chuỗi thời gian giao hàng ngắn gọn (ví dụ: "20 - 30 phút").
     */
    fun formatEta(
        restaurantLat: Double?,
        restaurantLon: Double?,
        userLat: Double?,
        userLon: Double?
    ): String {
        return estimate(restaurantLat, restaurantLon, userLat, userLon).durationText
    }

    /**
     * Định dạng chuỗi thời gian giao hàng kèm khoảng cách (ví dụ: "20 - 30 phút • 1.8 km").
     */
    fun formatEtaWithDistance(
        restaurantLat: Double?,
        restaurantLon: Double?,
        userLat: Double?,
        userLon: Double?
    ): String {
        val result = estimate(restaurantLat, restaurantLon, userLat, userLon)
        val distance = result.distanceKm
        return if (distance != null && distance > 0.0) {
            val formattedDistance = String.format(Locale.getDefault(), "%.1f", distance)
            "${result.durationText} • $formattedDistance km"
        } else {
            result.durationText
        }
    }

    private fun hasValidCoordinates(lat: Double?, lon: Double?): Boolean {
        return lat != null && lon != null && !(lat == 0.0 && lon == 0.0) &&
            !lat.isNaN() && !lon.isNaN() && !lat.isInfinite() && !lon.isInfinite()
    }
}
