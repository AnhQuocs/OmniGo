package com.example.omnigo.core.location.tracker

import android.content.Context
import com.example.omnigo.BuildConfig
import com.example.omnigo.core.location.model.UserLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacesSearchService @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val mapboxToken: String
        get() = BuildConfig.MAPBOX_ACCESS_TOKEN.trim()

    suspend fun searchPlaces(
        query: String,
        proximityLat: Double? = null,
        proximityLng: Double? = null
    ): List<UserLocation> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()
        if (mapboxToken.isBlank()) return@withContext emptyList()

        try {
            val sessionToken = UUID.randomUUID().toString()
            val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
            val proximityParam = if (proximityLat != null && proximityLng != null) {
                "&proximity=$proximityLng,$proximityLat"
            } else ""

            val url = "https://api.mapbox.com/search/searchbox/v1/suggest" +
                "?q=$encodedQuery&country=vn&language=vi$proximityParam" +
                "&session_token=$sessionToken&access_token=$mapboxToken"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "OmniGo-App/1.0")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return@withContext emptyList()

            val json = JSONObject(bodyString)
            val suggestions = json.optJSONArray("suggestions") ?: return@withContext emptyList()

            val candidates = mutableListOf<MapboxSuggestion>()
            for (i in 0 until suggestions.length()) {
                val item = suggestions.optJSONObject(i) ?: continue
                val featureType = item.optString("feature_type")
                if (featureType.equals("category", ignoreCase = true)) continue

                val mapboxId = item.optString("mapbox_id").trim()
                if (mapboxId.isBlank()) continue

                val name = item.optString("name").trim()
                val fullAddress = item.optString("full_address").trim()
                    .ifBlank { item.optString("place_formatted").trim() }
                    .ifBlank { name }

                candidates.add(
                    MapboxSuggestion(
                        mapboxId = mapboxId,
                        name = name,
                        fullAddress = fullAddress
                    )
                )
                if (candidates.size >= 6) break
            }

            coroutineScope {
                candidates.map { candidate ->
                    async {
                        val coords = retrievePlaceCoordinates(candidate.mapboxId, sessionToken)
                        if (coords != null) {
                            UserLocation(
                                latitude = coords.first,
                                longitude = coords.second,
                                addressName = candidate.name.ifBlank { trimmed },
                                fullAddress = candidate.fullAddress
                            )
                        } else null
                    }
                }.awaitAll().filterNotNull()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun retrievePlaceCoordinates(
        mapboxId: String,
        sessionToken: String
    ): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (mapboxId.isBlank() || mapboxToken.isBlank()) return@withContext null
        try {
            val url = "https://api.mapbox.com/search/searchbox/v1/retrieve/$mapboxId" +
                "?session_token=$sessionToken&access_token=$mapboxToken"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "OmniGo-App/1.0")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return@withContext null

            val json = JSONObject(bodyString)
            val features = json.optJSONArray("features") ?: return@withContext null
            val firstFeature = features.optJSONObject(0) ?: return@withContext null
            val geometry = firstFeature.optJSONObject("geometry") ?: return@withContext null
            val coordinates = geometry.optJSONArray("coordinates") ?: return@withContext null
            if (coordinates.length() < 2) return@withContext null

            val lon = coordinates.optDouble(0)
            val lat = coordinates.optDouble(1)
            if (lat == 0.0 && lon == 0.0) null else Pair(lat, lon)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        if (mapboxToken.isBlank()) return@withContext null
        try {
            val url = "https://api.mapbox.com/search/searchbox/v1/reverse" +
                "?longitude=$longitude&latitude=$latitude&types=poi,address&country=vn&language=vi&access_token=$mapboxToken"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "OmniGo-App/1.0")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return@withContext null

            val json = JSONObject(bodyString)
            val features = json.optJSONArray("features") ?: return@withContext null
            if (features.length() == 0) return@withContext null

            var poiName: String? = null
            var defaultAddress: String? = null

            for (i in 0 until features.length()) {
                val feature = features.optJSONObject(i) ?: continue
                val props = feature.optJSONObject("properties") ?: continue
                val featureType = props.optString("feature_type")
                val name = props.optString("name").trim()
                val fullAddress = props.optString("full_address").trim()
                    .ifBlank { props.optString("place_formatted").trim() }

                if (defaultAddress == null && fullAddress.isNotBlank()) {
                    defaultAddress = fullAddress
                }

                if (featureType.equals("poi", ignoreCase = true) && name.isNotBlank() && poiName == null) {
                    poiName = name
                    if (fullAddress.isNotBlank()) {
                        defaultAddress = fullAddress
                    }
                }
            }

            val finalName = poiName ?: features.optJSONObject(0)?.optJSONObject("properties")?.optString("name")?.trim().orEmpty()
            val finalAddress = defaultAddress ?: finalName

            if (finalName.isNotBlank() || finalAddress.isNotBlank()) {
                Pair(
                    finalName.ifBlank { finalAddress },
                    finalAddress.ifBlank { finalName }
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private data class MapboxSuggestion(
        val mapboxId: String,
        val name: String,
        val fullAddress: String
    )
}
