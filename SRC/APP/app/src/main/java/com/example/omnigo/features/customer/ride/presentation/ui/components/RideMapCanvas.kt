package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.features.customer.ride.domain.model.NearbyDriver
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.omnigo.R
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor

private const val DEFAULT_LATITUDE = 10.776889
private const val DEFAULT_LONGITUDE = 106.700806
private const val DEFAULT_ZOOM = 15f
private const val BOUNDS_PADDING = 140
private const val FOLLOW_ZOOM = 17f
private const val FOLLOW_MIN_ZOOM = 16f

private fun createVehicleMarkerBitmap(context: Context, resId: Int, maxWidthPx: Int, maxHeightPx: Int): BitmapDescriptor? {
    return try {
        try {
            MapsInitializer.initialize(context)
        } catch (e: Exception) {
            android.util.Log.w("RideMapCanvas", "MapsInitializer failed: ${e.message}")
        }
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
    } catch (e: Exception) {
        android.util.Log.e("RideMapCanvas", "Failed to create vehicle marker for $resId: ${e.message}", e)
        null
    }
}

@Composable
fun RideMapCanvas(
    pickupLocation: LocationPoint?,
    dropoffLocation: LocationPoint?,
    nearbyDrivers: List<NearbyDriver>,
    selectedVehicleType: VehicleType,
    routeCoordinates: List<Pair<Double, Double>> = emptyList(),
    driverLiveLocation: DriverLiveLocation? = null,
    tripStatus: BookingStatus? = null,
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
        val initialLat = pickupLocation?.latitude ?: DEFAULT_LATITUDE
        val initialLng = pickupLocation?.longitude ?: DEFAULT_LONGITUDE
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), DEFAULT_ZOOM)
    },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val markerWidth = with(density) { Dimen.SizeVehicleMarkerWidth.roundToPx() }
    val markerHeight = with(density) { Dimen.SizeVehicleMarkerHeight.roundToPx() }

    var bikeMarker by remember(context, markerWidth, markerHeight) {
        mutableStateOf(createVehicleMarkerBitmap(context, R.drawable.marker_omnibike, markerWidth, markerHeight))
    }
    var car4Marker by remember(context, markerWidth, markerHeight) {
        mutableStateOf(createVehicleMarkerBitmap(context, R.drawable.marker_omnicar4, markerWidth, markerHeight))
    }
    var car7Marker by remember(context, markerWidth, markerHeight) {
        mutableStateOf(createVehicleMarkerBitmap(context, R.drawable.marker_omnicar7, markerWidth, markerHeight))
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            zoomGesturesEnabled = true
        )
    }

    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false
        )
    }

    // Camera animation according to trip phase or selection phase
    LaunchedEffect(pickupLocation, dropoffLocation, driverLiveLocation, tripStatus) {
        recenterMapCamera(
            cameraPositionState = cameraPositionState,
            driverLocation = driverLiveLocation,
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation,
            isInProgress = tripStatus == BookingStatus.IN_PROGRESS
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties,
            onMapLoaded = {
                if (bikeMarker == null) bikeMarker = createVehicleMarkerBitmap(context, R.drawable.marker_omnibike, markerWidth, markerHeight)
                if (car4Marker == null) car4Marker = createVehicleMarkerBitmap(context, R.drawable.marker_omnicar4, markerWidth, markerHeight)
                if (car7Marker == null) car7Marker = createVehicleMarkerBitmap(context, R.drawable.marker_omnicar7, markerWidth, markerHeight)
            },
            contentPadding = PaddingValues(
                top = Dimen.SizeUltra,
                bottom = Dimen.SizeUltra * 2
            )
        ) {
            // Pickup Marker
            pickupLocation?.let { pickup ->
                Marker(
                    state = MarkerState(position = LatLng(pickup.latitude, pickup.longitude)),
                    title = pickup.name.ifBlank { "Điểm đón" },
                    snippet = pickup.address,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // Dropoff Marker
            dropoffLocation?.let { dropoff ->
                Marker(
                    state = MarkerState(position = LatLng(dropoff.latitude, dropoff.longitude)),
                    title = dropoff.name.ifBlank { "Điểm đến" },
                    snippet = dropoff.address,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }

            // Live Driver Location Marker with rotation (Heading)
            driverLiveLocation?.let { liveLoc ->
                val liveMarkerIcon = when (selectedVehicleType) {
                    VehicleType.BIKE, VehicleType.EXPRESS -> bikeMarker
                    VehicleType.CAR_4_SEAT -> car4Marker
                    VehicleType.CAR_7_SEAT -> car7Marker
                } ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)

                Marker(
                    state = MarkerState(position = LatLng(liveLoc.latitude, liveLoc.longitude)),
                    title = "Tài xế đang di chuyển",
                    snippet = "Tốc độ: ${liveLoc.speed} km/h",
                    rotation = liveLoc.bearing ?: 0f,
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                    icon = liveMarkerIcon
                )
            }

            // Route polyline connecting points
            if (routeCoordinates.isNotEmpty()) {
                Polyline(
                    points = routeCoordinates.map { LatLng(it.first, it.second) },
                    color = if (tripStatus == BookingStatus.IN_PROGRESS) SecondaryColor else PrimaryColor,
                    width = 12f
                )
            }

            // Nearby drivers markers when no active trip or pending match
            if (tripStatus == null || tripStatus == BookingStatus.PENDING) {
                nearbyDrivers.forEach { driver ->
                    val markerIcon = when (driver.vehicleType) {
                        VehicleType.BIKE, VehicleType.EXPRESS -> bikeMarker
                        VehicleType.CAR_4_SEAT -> car4Marker
                        VehicleType.CAR_7_SEAT -> car7Marker
                        null -> bikeMarker
                    } ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)

                    Marker(
                        state = MarkerState(position = LatLng(driver.latitude, driver.longitude)),
                        title = "Tài xế OmniGo #${driver.driverId}",
                        snippet = "Cách bạn ~${String.format(java.util.Locale.US, "%.1f", driver.distanceInKm)} km",
                        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                        icon = markerIcon
                    )
                }
            }
        }
    }
}

suspend fun recenterMapCamera(
    cameraPositionState: CameraPositionState,
    driverLocation: DriverLiveLocation?,
    pickupLocation: LocationPoint?,
    dropoffLocation: LocationPoint?,
    isInProgress: Boolean
) {
    if (isInProgress) {
        val target = driverLocation?.let { LatLng(it.latitude, it.longitude) }
            ?: pickupLocation?.let { LatLng(it.latitude, it.longitude) }
            ?: dropoffLocation?.let { LatLng(it.latitude, it.longitude) }
        target?.let { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 17f)) }
    } else if (driverLocation != null && pickupLocation != null) {
        val bounds = LatLngBounds.builder()
            .include(LatLng(driverLocation.latitude, driverLocation.longitude))
            .include(LatLng(pickupLocation.latitude, pickupLocation.longitude))
            .build()
        try {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 140))
        } catch (_: Exception) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(driverLocation.latitude, driverLocation.longitude), 17f))
        }
    } else if (pickupLocation != null && dropoffLocation != null) {
        val bounds = LatLngBounds.builder()
            .include(LatLng(pickupLocation.latitude, pickupLocation.longitude))
            .include(LatLng(dropoffLocation.latitude, dropoffLocation.longitude))
            .build()
        try {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 140))
        } catch (_: Exception) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(pickupLocation.latitude, pickupLocation.longitude), 15f))
        }
    } else if (pickupLocation != null) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(pickupLocation.latitude, pickupLocation.longitude), 15f))
    }
}

