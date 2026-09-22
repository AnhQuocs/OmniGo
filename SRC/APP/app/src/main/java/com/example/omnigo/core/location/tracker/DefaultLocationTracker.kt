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
    @param:ApplicationContext private val context: Context
) : LocationTracker {

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
        if (freshHighAcc != null) return freshHighAcc

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
        if (balancedLocation != null) return balancedLocation

        // 3. Fallback to last known location
        return kotlinx.coroutines.withTimeoutOrNull(2000L) {
            suspendCancellableCoroutine { continuation ->
                locationClient.lastLocation
                    .addOnSuccessListener { loc -> if (continuation.isActive) continuation.resume(loc) }
                    .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
                    .addOnCanceledListener { if (continuation.isActive) continuation.cancel() }
            }
        }
    }

    private suspend fun resolveAddressDetails(latitude: Double, longitude: Double): Pair<String, String> {
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
                            "Vị trí GPS (${String.format(Locale.US, "%.4f, %.4f", latitude, longitude)})"
                        }
                        Pair(short, full)
                    }
                } else {
                    Pair(
                        "Vị trí GPS (${String.format(Locale.US, "%.4f, %.4f", latitude, longitude)})",
                        "Tọa độ: ${String.format(Locale.US, "%.5f, %.5f", latitude, longitude)}"
                    )
                }
            } catch (e: Exception) {
                Pair(
                    "Vị trí GPS (${String.format(Locale.US, "%.4f, %.4f", latitude, longitude)})",
                    "Tọa độ: ${String.format(Locale.US, "%.5f, %.5f", latitude, longitude)}"
                )
            }
        }
    }

    private fun sanitizeAddress(rawFullAddress: String): String {
        var clean = rawFullAddress
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
