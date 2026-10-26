package com.example.omnigo.core.location.util

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

suspend fun reverseGeocode(
    context: Context,
    latitude: Double,
    longitude: Double
): Pair<String, String> = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            val name = addr.featureName ?: addr.thoroughfare ?: addr.subLocality ?: "Điểm đã chọn"
            val full = addr.getAddressLine(0) ?: "$latitude, $longitude"
            return@withContext Pair(name, full)
        }
    } catch (_: Exception) {
    }
    Pair("Vị trí ghim", String.format(Locale.US, "Tọa độ: %.5f, %.5f", latitude, longitude))
}
