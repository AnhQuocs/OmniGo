package com.example.omnigo.core.location.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.omnigo.features.driver.domain.model.DriverLocationUpdate
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverNavigationLocationSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {
        const val NAVIGATION_INTERVAL_MS = 1500L
        const val NAVIGATION_MIN_INTERVAL_MS = 1000L
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    fun observeLocationUpdates(): Flow<DriverLocationUpdate> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            NAVIGATION_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(NAVIGATION_MIN_INTERVAL_MS)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                trySend(
                    DriverLocationUpdate(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        bearing = if (location.hasBearing()) location.bearing else null,
                        speed = if (location.hasSpeed()) location.speed else null,
                        timestamp = location.time
                    )
                )
            }
        }

        try {
            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (lastLoc != null) {
                    trySend(
                        DriverLocationUpdate(
                            latitude = lastLoc.latitude,
                            longitude = lastLoc.longitude,
                            bearing = if (lastLoc.hasBearing()) lastLoc.bearing else null,
                            speed = if (lastLoc.hasSpeed()) lastLoc.speed else null,
                            timestamp = lastLoc.time
                        )
                    )
                }
            }
            fusedClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            close()
            return@callbackFlow
        }

        awaitClose {
            try {
                fusedClient.removeLocationUpdates(callback)
            } catch (_: Exception) {
                // Ignored
            }
        }
    }
}
