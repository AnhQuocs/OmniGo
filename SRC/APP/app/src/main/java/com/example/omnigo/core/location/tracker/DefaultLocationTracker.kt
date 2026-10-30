package com.example.omnigo.core.location.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.omnigo.core.location.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class DefaultLocationTracker @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    @param:ApplicationContext private val context: Context,
    private val placesSearchService: PlacesSearchService
) : LocationTracker {

    @Volatile
    private var cachedLastLocation: Pair<Double, Double>? = null

    override fun isLocationPermissionGranted(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    override fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): UserLocation? {
        if (!isLocationPermissionGranted() || !isGpsEnabled()) {
            return null
        }

        return withContext(Dispatchers.IO) {
            val location = fetchCurrentAndroidLocation() ?: return@withContext null
            val addressInfo = resolveAddressDetails(location.latitude, location.longitude)

            UserLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                addressName = addressInfo.first,
                fullAddress = addressInfo.second
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchCurrentAndroidLocation(): Location? {
        val cancellationTokenSource = CancellationTokenSource()

        // 1. Try High Accuracy GPS with 6s timeout
        val freshHighAcc = kotlinx.coroutines.withTimeoutOrNull(6000L) {
            suspendCancellableCoroutine<Location?> { continuation ->
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }.addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }.addOnCanceledListener {
                    if (continuation.isActive) continuation.cancel()
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        }
        if (freshHighAcc != null) {
            cachedLastLocation = Pair(freshHighAcc.latitude, freshHighAcc.longitude)
            return freshHighAcc
        }

        // 2. Try Balanced Power Accuracy (WiFi / Cell) with 3s timeout for fast indoor fix
        val balancedCts = CancellationTokenSource()
        val balancedLocation = kotlinx.coroutines.withTimeoutOrNull(3000L) {
            suspendCancellableCoroutine<Location?> { continuation ->
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    balancedCts.token
                ).addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }.addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }.addOnCanceledListener {
                    if (continuation.isActive) continuation.cancel()
                }

                continuation.invokeOnCancellation {
                    balancedCts.cancel()
                }
            }
        }
        if (balancedLocation != null) {
            cachedLastLocation = Pair(balancedLocation.latitude, balancedLocation.longitude)
            return balancedLocation
        }

        // 3. Fallback to last known location
        val lastLoc = kotlinx.coroutines.withTimeoutOrNull(2000L) {
            suspendCancellableCoroutine { continuation ->
                locationClient.lastLocation
                    .addOnSuccessListener { loc -> if (continuation.isActive) continuation.resume(loc) }
                    .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
                    .addOnCanceledListener { if (continuation.isActive) continuation.cancel() }
            }
        }
        if (lastLoc != null) {
            cachedLastLocation = Pair(lastLoc.latitude, lastLoc.longitude)
        }
        return lastLoc
    }

    private suspend fun resolveAddressDetails(latitude: Double, longitude: Double): Pair<String, String> {
        val mapboxResult = placesSearchService.reverseGeocode(latitude, longitude)
        if (mapboxResult != null && mapboxResult.first.isNotBlank()) {
            return mapboxResult
        }

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.forLanguageTag("vi-VN"))
                val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            continuation.resume(addresses.firstOrNull())
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                }

                if (address != null) {
                    val rawLine0 = address.getAddressLine(0)?.trim().orEmpty()
                    val cleanFull = sanitizeAddress(rawLine0)

                    if (cleanFull.isNotBlank()) {
                        val shortName = extractShortAddress(cleanFull)
                        Pair(shortName, cleanFull)
                    } else {
                        val parts = listOfNotNull(
                            address.subLocality,
                            address.subAdminArea,
                            address.adminArea
                        ).filter { it.isNotBlank() }.distinct()

                        val full = if (parts.isNotEmpty()) {
                            parts.joinToString(", ")
                        } else {
                            "Tọa độ: ${String.format(Locale.US, "%.5f, %.5f", latitude, longitude)}"
                        }
                        val short = if (parts.isNotEmpty()) {
                            extractShortAddress(full)
                        } else {
                            resolveHanoiLocationFallback(latitude, longitude).first
                        }
                        val finalFull = if (parts.isNotEmpty()) full else resolveHanoiLocationFallback(latitude, longitude).second
                        Pair(short, finalFull)
                    }
                } else {
                    resolveHanoiLocationFallback(latitude, longitude)
                }
            } catch (e: Exception) {
                resolveHanoiLocationFallback(latitude, longitude)
            }
        }
    }

    private fun resolveHanoiLocationFallback(latitude: Double, longitude: Double): Pair<String, String> {
        val latStr = String.format(Locale.US, "%.4f", latitude)
        val lngStr = String.format(Locale.US, "%.4f", longitude)
        val key = "$latStr,$lngStr"
        val known = mapOf(
            "21.0185,105.8266" to "Hoàng Cầu, Đống Đa, Hà Nội",
            "21.0245,105.8574" to "Tràng Tiền, Hoàn Kiếm, Hà Nội",
            "21.0585,105.8202" to "Xuân La, Tây Hồ, Hà Nội",
            "21.0285,105.8542" to "Đinh Tiên Hoàng, Hoàn Kiếm, Hà Nội",
            "21.0031,105.8431" to "Đại Cồ Việt, Hai Bà Trưng, Hà Nội",
            "21.0378,105.7836" to "Duy Tân, Cầu Giấy, Hà Nội",
            "21.0135,105.7988" to "Trung Hòa, Cầu Giấy, Hà Nội",
            "21.0333,105.8500" to "Điện Biên Phủ, Ba Đình, Hà Nội",
            "21.0307,105.7845" to "Trần Thái Tông, Cầu Giấy, Hà Nội",
            "21.0175,105.8540" to "Phố Huế, Hai Bà Trưng, Hà Nội"
        )
        if (known.containsKey(key)) {
            val addr = known[key]!!
            return Pair(extractShortAddress(addr), addr)
        }
        if (latitude in 20.9..21.2 && longitude in 105.7..106.0) {
            val district = when {
                latitude > 21.05 -> "Tây Hồ, Hà Nội"
                latitude > 21.03 -> "Ba Đình, Hà Nội"
                longitude < 105.80 -> "Cầu Giấy, Hà Nội"
                longitude in 105.815..105.845 && latitude in 21.005..21.030 -> "Hoàng Cầu, Đống Đa, Hà Nội"
                latitude < 21.01 -> "Hai Bà Trưng, Hà Nội"
                else -> "Hoàn Kiếm, Hà Nội"
            }
            return Pair(district, district)
        }
        val defaultName = "Vị trí (${String.format(Locale.US, "%.4f, %.4f", latitude, longitude)})"
        return Pair(defaultName, defaultName)
    }

    private fun sanitizeAddress(rawFullAddress: String): String {
        var clean = rawFullAddress
            .replace(Regex("^[A-Z0-9]{4,8}\\+[A-Z0-9]{2,4}(,\\s*)?"), "")
            .replace(Regex(",?\\s*Việt Nam$", RegexOption.IGNORE_CASE), "")
            .replace(Regex(",?\\s*Vietnam$", RegexOption.IGNORE_CASE), "")
            .trim()

        val poiKeywords = listOf(
            "Hợp tác xã", "Hợp Tác Xã", "HTX",
            "Công ty", "Công Ty", "TNHH", "Cổ phần", "Cổ Phần",
            "Cửa hàng", "Cửa Hàng", "Shop",
            "Nhà xe", "Nhà Xe", "Bến xe", "Bến Xe",
            "Trụ sở", "Trụ Sở", "Văn phòng", "Văn Phòng",
            "Tòa nhà", "Tòa Nhà", "Chung cư", "Chung Cư",
            "Ủy ban", "UBND"
        )

        val parts = clean.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (parts.size >= 3) {
            val firstPart = parts[0]
            val isPoi = poiKeywords.any { firstPart.startsWith(it, ignoreCase = true) }
            if (isPoi) {
                clean = parts.drop(1).joinToString(", ")
            }
        }

        return clean
    }

    override suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String? {
        val details = resolveAddressDetails(latitude, longitude)
        return details.second.ifBlank { null }
    }

    override suspend fun getCoordinatesFromAddress(address: String): Pair<Double, Double>? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocationName(address, 1) { addresses ->
                            val first = addresses.firstOrNull()
                            if (first != null) {
                                continuation.resume(Pair(first.latitude, first.longitude))
                            } else {
                                continuation.resume(null)
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(address, 1)
                    val first = addresses?.firstOrNull()
                    if (first != null) {
                        Pair(first.latitude, first.longitude)
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun searchPlaces(query: String): List<UserLocation> {
        return searchPlaces(query, null, null)
    }

    override suspend fun searchPlaces(
        query: String,
        proximityLat: Double?,
        proximityLng: Double?
    ): List<UserLocation> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()

        val lat = proximityLat ?: cachedLastLocation?.first
        val lng = proximityLng ?: cachedLastLocation?.second

        // 1. Priority: Mapbox Search Box v1 (Suggest + Retrieve)
        val mapboxResults = placesSearchService.searchPlaces(trimmed, lat, lng)
        if (mapboxResults.isNotEmpty()) {
            return mapboxResults
        }

        // 2. Fallback: Android Geocoder
        return searchPlacesWithGeocoder(trimmed)
    }

    private suspend fun searchPlacesWithGeocoder(query: String): List<UserLocation> = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.forLanguageTag("vi-VN"))
            val searchTarget = if (!query.contains("Việt Nam", true) && !query.contains("Vietnam", true)) {
                "$query, Việt Nam"
            } else query

            var addresses: List<android.location.Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocationName(searchTarget, 8, 8.18, 102.14, 23.40, 109.50) { results ->
                        cont.resume(results ?: emptyList())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(searchTarget, 8, 8.18, 102.14, 23.40, 109.50) ?: emptyList()
            }

            if (addresses.isEmpty()) {
                addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocationName(query, 8) { results ->
                            cont.resume(results ?: emptyList())
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(query, 8) ?: emptyList()
                }
            }

            addresses.mapNotNull { address ->
                val lat = address.latitude
                val lng = address.longitude
                if (lat == 0.0 && lng == 0.0) return@mapNotNull null
                if (address.countryCode != null && !address.countryCode.equals("VN", ignoreCase = true)) return@mapNotNull null

                val fullAddress = address.getAddressLine(0) ?: "$lat, $lng"
                val rawFeature = address.featureName
                val isGenericStreet = rawFeature != null && fullAddress.startsWith(rawFeature, ignoreCase = true)
                val shortName = if (!isGenericStreet && !rawFeature.isNullOrBlank()) {
                    rawFeature
                } else {
                    extractShortAddress(fullAddress)
                }

                UserLocation(
                    latitude = lat,
                    longitude = lng,
                    addressName = shortName.ifBlank { query },
                    fullAddress = fullAddress
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun extractShortAddress(fullAddress: String): String {
        val clean = sanitizeAddress(fullAddress)
        if (clean.isBlank()) return ""
        val parts = clean.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (parts.isEmpty()) return clean

        if (parts[0].matches(Regex("^[0-9]+[a-zA-Z0-9/\\-]*$")) && parts.size >= 2) {
            return if (parts.size >= 3) {
                "${parts[0]} ${parts[1]}, ${parts[2]}"
            } else {
                "${parts[0]} ${parts[1]}"
            }
        }

        return when {
            parts.size >= 2 -> "${parts[0]}, ${parts[1]}"
            parts.size == 1 -> parts[0]
            else -> clean
        }
    }
}
