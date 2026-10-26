package com.example.omnigo.core.location.tracker

import android.content.Context
import com.example.omnigo.features.customer.ride.data.remote.api.PricingApi
import com.example.omnigo.features.customer.ride.data.remote.dto.RouteRequest
import com.example.omnigo.utils.PolylineDecoder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OsrmRoutingService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pricingApi: PricingApi
) {
    /**
     * Fetches driving route coordinates directly from OmniGo Backend (/api/v1/pricing/route).
     * Decodes the returned encodedPolyline using Google-encoded polyline algorithm.
     */
    suspend fun getRouteCoordinates(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        vehicleType: String = "BIKE"
    ): List<Pair<Double, Double>> = withContext(Dispatchers.IO) {
        if (startLat == 0.0 || startLng == 0.0 || endLat == 0.0 || endLng == 0.0) {
            return@withContext emptyList()
        }

        try {
            val response = pricingApi.getRoute(
                RouteRequest(
                    startLatitude = startLat,
                    startLongitude = startLng,
                    endLatitude = endLat,
                    endLongitude = endLng,
                    vehicleType = vehicleType
                )
            )
            val decoded = PolylineDecoder.decode(response.encodedPolyline)
            android.util.Log.d(
                "OsrmRoutingService",
                "Backend route for $vehicleType returned ${decoded.size} points from source: ${response.source}"
            )
            decoded
        } catch (e: Exception) {
            android.util.Log.e("OsrmRoutingService", "Backend route call failed: ${e.message}", e)
            emptyList()
        }
    }
}
