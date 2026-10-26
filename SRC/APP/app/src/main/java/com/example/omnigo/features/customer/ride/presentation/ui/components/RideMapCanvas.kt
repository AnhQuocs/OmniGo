package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.domain.model.NearbyDriver
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
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
import com.google.maps.android.compose.rememberCameraPositionState

private const val DEFAULT_LATITUDE = 10.776889
private const val DEFAULT_LONGITUDE = 106.700806
private const val DEFAULT_ZOOM = 15f
private const val BOUNDS_PADDING = 120

@Composable
fun RideMapCanvas(
    pickupLocation: LocationPoint?,
    dropoffLocation: LocationPoint?,
    nearbyDrivers: List<NearbyDriver>,
    selectedVehicleType: VehicleType,
    modifier: Modifier = Modifier
) {
    val initialLat = pickupLocation?.latitude ?: DEFAULT_LATITUDE
    val initialLng = pickupLocation?.longitude ?: DEFAULT_LONGITUDE

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), DEFAULT_ZOOM)
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

    // Automatically zoom to fit both pickup and dropoff if both exist
    LaunchedEffect(pickupLocation, dropoffLocation) {
        val pickup = pickupLocation
        val dropoff = dropoffLocation
        if (pickup != null && dropoff != null) {
            val bounds = LatLngBounds.builder()
                .include(LatLng(pickup.latitude, pickup.longitude))
                .include(LatLng(dropoff.latitude, dropoff.longitude))
                .build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, BOUNDS_PADDING))
        } else if (pickup != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(pickup.latitude, pickup.longitude),
                    DEFAULT_ZOOM
                )
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties,
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

            // Direct route polyline connecting Pickup and Dropoff
            if (pickupLocation != null && dropoffLocation != null) {
                Polyline(
                    points = listOf(
                        LatLng(pickupLocation.latitude, pickupLocation.longitude),
                        LatLng(dropoffLocation.latitude, dropoffLocation.longitude)
                    ),
                    color = PrimaryColor,
                    width = 8f
                )
            }

            // Nearby drivers markers
            nearbyDrivers.forEach { driver ->
                Marker(
                    state = MarkerState(position = LatLng(driver.latitude, driver.longitude)),
                    title = "Tài xế #${driver.driverId}",
                    snippet = "${driver.distanceInKm} km",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }
        }
    }
}
