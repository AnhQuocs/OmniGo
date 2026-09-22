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

        val freshLocation = kotlinx.coroutines.withTimeoutOrNull(4000L) {
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

        if (freshLocation != null) return freshLocation

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
                    val cleanFull = rawLine0
                        .replace(Regex(",?\\s*Việt Nam$", RegexOption.IGNORE_CASE), "")
                        .replace(Regex(",?\\s*Vietnam$", RegexOption.IGNORE_CASE), "")
                        .trim()

                    val shortName = if (!address.thoroughfare.isNullOrBlank()) {
                        val street = if (!address.subThoroughfare.isNullOrBlank()) {
                            "${address.subThoroughfare.trim()} ${address.thoroughfare.trim()}"
                        } else {
                            address.thoroughfare.trim()
                        }
                        val districtOrWard = address.subAdminArea?.trim()
                            ?: address.subLocality?.trim()
                            ?: address.adminArea?.trim()
                            ?: ""
                        if (districtOrWard.isNotBlank()) "$street, $districtOrWard" else street
                    } else if (cleanFull.isNotBlank()) {
                        extractShortAddress(cleanFull)
                    } else {
                        "Vị trí GPS (${String.format(Locale.US, "%.4f, %.4f", latitude, longitude)})"
                    }

                    val full = if (cleanFull.isNotBlank()) {
                        cleanFull
                    } else {
                        "Tọa độ: ${String.format(Locale.US, "%.5f, %.5f", latitude, longitude)}"
                    }

                    Pair(shortName, full)
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
        if (fullAddress.isBlank()) return ""
        val parts = fullAddress.split(",")
        return if (parts.size >= 2) {
            "${parts[0].trim()}, ${parts[1].trim()}"
        } else {
            fullAddress
        }
    }
}
