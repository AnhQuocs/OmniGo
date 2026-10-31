package com.example.omnigo.features.driver.home.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

private const val DEFAULT_DRIVER_LAT = 10.776889
private const val DEFAULT_DRIVER_LNG = 106.700806
private const val DEFAULT_DRIVER_ZOOM = 16.5f

@Composable
fun DriverHomeRadarMap(
    currentLatitude: Double?,
    currentLongitude: Double?,
    isOnline: Boolean,
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier
) {
    val targetLat = currentLatitude ?: DEFAULT_DRIVER_LAT
    val targetLng = currentLongitude ?: DEFAULT_DRIVER_LNG
    val driverLatLng = LatLng(targetLat, targetLng)

    LaunchedEffect(targetLat, targetLng) {
        runCatching {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(driverLatLng, DEFAULT_DRIVER_ZOOM)
            )
        }
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }
    val mapProperties = remember { MapProperties(isMyLocationEnabled = false) }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties,
            contentPadding = PaddingValues(top = Dimen.SizeUltra, bottom = Dimen.SizeUltra * 2)
        ) {
            // Subtle static location accuracy circle (No animation to prevent recomposition jank & marker blinking)
            Circle(
                center = driverLatLng,
                radius = 50.0,
                fillColor = PrimaryColor.copy(alpha = 0.12f),
                strokeColor = PrimaryColor.copy(alpha = 0.35f),
                strokeWidth = 2f
            )

            // Driver Marker
            Marker(
                state = MarkerState(position = driverLatLng),
                title = stringResource(id = R.string.driver_status_online),
                icon = BitmapDescriptorFactory.defaultMarker(
                    if (isOnline) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_AZURE
                )
            )
        }
    }
}
