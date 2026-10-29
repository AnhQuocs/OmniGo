package com.example.omnigo.features.driver.common.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.example.omnigo.features.driver.common.presentation.DriverNavigationUiState
import com.example.omnigo.features.driver.domain.util.RoutePolylineSnapper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState

enum class NavigationCameraMode {
    FOLLOW_3D,
    FOLLOW_2D,
    OVERVIEW,
    FREE_PAN
}

internal const val DEFAULT_MAP_LAT = 10.776889
internal const val DEFAULT_MAP_LNG = 106.700806
internal const val FOLLOW_ZOOM = 19.2f
internal const val FOLLOW_3D_TILT = 45.0f
internal const val FOLLOW_2D_TILT = 0.0f
private const val LOOK_AHEAD_3D_METERS = 28.0
private const val LOOK_AHEAD_2D_METERS = 16.0
private const val OVERVIEW_BOUNDS_PADDING = 160

suspend fun animateToFollow3D(
    cameraPositionState: CameraPositionState,
    lat: Double,
    lng: Double,
    bearing: Float
) {
    val lookAhead = RoutePolylineSnapper.computeLookAheadTarget(
        lat = lat,
        lng = lng,
        bearingDegrees = bearing,
        offsetMeters = LOOK_AHEAD_3D_METERS
    )
    val cameraPosition = CameraPosition.Builder()
        .target(LatLng(lookAhead.first, lookAhead.second))
        .zoom(FOLLOW_ZOOM)
        .tilt(FOLLOW_3D_TILT)
        .bearing(bearing)
        .build()

    runCatching {
        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            durationMs = 600
        )
    }
}

suspend fun animateToFollow2D(
    cameraPositionState: CameraPositionState,
    lat: Double,
    lng: Double,
    bearing: Float
) {
    val lookAhead = RoutePolylineSnapper.computeLookAheadTarget(
        lat = lat,
        lng = lng,
        bearingDegrees = bearing,
        offsetMeters = LOOK_AHEAD_2D_METERS
    )
    val cameraPosition = CameraPosition.Builder()
        .target(LatLng(lookAhead.first, lookAhead.second))
        .zoom(FOLLOW_ZOOM)
        .tilt(FOLLOW_2D_TILT)
        .bearing(bearing)
        .build()

    runCatching {
        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            durationMs = 600
        )
    }
}

suspend fun animateToOverview(
    cameraPositionState: CameraPositionState,
    navigationState: DriverNavigationUiState
) {
    val builder = LatLngBounds.builder()
    var count = 0

    if (navigationState.driverLat != null && navigationState.driverLng != null) {
        builder.include(LatLng(navigationState.driverLat, navigationState.driverLng))
        count++
    }
    if (navigationState.targetLat != null && navigationState.targetLng != null) {
        builder.include(LatLng(navigationState.targetLat, navigationState.targetLng))
        count++
    }
    if (navigationState.secondaryLat != null && navigationState.secondaryLng != null) {
        builder.include(LatLng(navigationState.secondaryLat, navigationState.secondaryLng))
        count++
    }
    navigationState.remainingPolyline.forEach { (lat, lng) ->
        builder.include(LatLng(lat, lng))
        count++
    }

    if (count >= 2) {
        runCatching {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(builder.build(), OVERVIEW_BOUNDS_PADDING)
            )
        }
    }
}

fun createDriverVehicleIcon(
    context: Context,
    resId: Int,
    maxWidthPx: Int,
    maxHeightPx: Int
): BitmapDescriptor? {
    return try {
        runCatching { MapsInitializer.initialize(context) }
        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        val intrinsicWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: maxWidthPx
        val intrinsicHeight = drawable.intrinsicHeight.takeIf { it > 0 } ?: maxHeightPx
        val ratio = intrinsicWidth.toFloat() / intrinsicHeight.toFloat()
        val (finalWidth, finalHeight) = if (ratio > 1f) {
            maxWidthPx to (maxWidthPx / ratio).toInt().coerceAtLeast(1)
        } else {
            (maxHeightPx * ratio).toInt().coerceAtLeast(1) to maxHeightPx
        }
        val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (_: Exception) {
        null
    }
}
