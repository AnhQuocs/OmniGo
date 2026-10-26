package com.example.omnigo.core.location.tracker

import android.content.Context
import com.example.omnigo.core.location.model.UserLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
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

    suspend fun searchPlaces(query: String): List<UserLocation> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        try {
            val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
            // Vietnam Bounding Box: 102.14 MinLng, 8.18 MinLat, 109.50 MaxLng, 23.40 MaxLat
            val url = "https://photon.komoot.io/api/?q=$encodedQuery&limit=8&bbox=102.14,8.18,109.50,23.40&lang=default"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "OmniGo-App/1.0")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("PlacesSearchService", "Photon search failed: HTTP ${response.code}")
                return@withContext emptyList()
            }

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return@withContext emptyList()

            val jsonObject = JSONObject(bodyString)
            val features = jsonObject.optJSONArray("features") ?: return@withContext emptyList()

            val results = mutableListOf<UserLocation>()

            for (i in 0 until features.length()) {
                val feature = features.optJSONObject(i) ?: continue
                val geometry = feature.optJSONObject("geometry") ?: continue
                val coordinates = geometry.optJSONArray("coordinates") ?: continue
                if (coordinates.length() < 2) continue

                val lon = coordinates.optDouble(0)
                val lat = coordinates.optDouble(1)
                if (lat == 0.0 && lon == 0.0) continue

                val props = feature.optJSONObject("properties") ?: continue
                val countryCode = props.optString("countrycode").trim()
                if (countryCode.isNotBlank() && !countryCode.equals("VN", ignoreCase = true)) {
                    continue
                }

                val name = props.optString("name").trim()
                val street = props.optString("street").trim()
                val houseNumber = props.optString("housenumber").trim()
                val district = props.optString("district").trim()
                val city = props.optString("city").trim()
                val country = props.optString("country").trim().ifBlank { "Việt Nam" }

                val streetAddress = when {
                    houseNumber.isNotBlank() && street.isNotBlank() -> "$houseNumber $street"
                    street.isNotBlank() -> street
                    else -> ""
                }

                val addressName = when {
                    name.isNotBlank() -> name
                    streetAddress.isNotBlank() -> streetAddress
                    else -> trimmed
                }

                val addressParts = listOfNotNull(
                    if (name.isNotBlank() && streetAddress.isNotBlank()) streetAddress else null,
                    district.ifBlank { null },
                    city.ifBlank { null },
                    country.ifBlank { null }
                ).filter { it.isNotBlank() }

                val fullAddress = if (addressParts.isNotEmpty()) {
                    addressParts.joinToString(", ")
                } else {
                    addressName
                }

                results.add(
                    UserLocation(
                        latitude = lat,
                        longitude = lon,
                        addressName = addressName,
                        fullAddress = fullAddress
                    )
                )
            }

            android.util.Log.d("PlacesSearchService", "Photon search for '$trimmed' returned ${results.size} places")
            results
        } catch (e: Exception) {
            android.util.Log.e("PlacesSearchService", "Photon search error", e)
            emptyList()
        }
    }
}
