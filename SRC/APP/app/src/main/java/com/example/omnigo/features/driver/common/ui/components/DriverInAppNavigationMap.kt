package com.example.omnigo.features.driver.common.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.example.omnigo.R
import com.example.omnigo.features.driver.common.presentation.DriverNavigationUiState
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s12
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private const val POLYLINE_WIDTH_PX = 18f

@Composable
fun DriverInAppNavigationMap(
    navigationState: DriverNavigationUiState,
    modifier: Modifier = Modifier,
    bottomSheetPadding: Dp = Dimen.HeightPinPickerBottomOffset
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val markerWidth = with(density) { Dimen.SizeVehicleMarkerWidth.roundToPx() }
    val markerHeight = with(density) { Dimen.SizeVehicleMarkerHeight.roundToPx() }

    var bikeIcon by remember(context, markerWidth, markerHeight) {
        mutableStateOf(createDriverVehicleIcon(context, R.drawable.marker_omnibike, markerWidth, markerHeight))
    }
    var car4Icon by remember(context, markerWidth, markerHeight) {
        mutableStateOf(createDriverVehicleIcon(context, R.drawable.marker_omnicar4, markerWidth, markerHeight))
    }
    var car7Icon by remember(context, markerWidth, markerHeight) {
        mutableStateOf(createDriverVehicleIcon(context, R.drawable.marker_omnicar7, markerWidth, markerHeight))
    }

    var cameraMode by remember { mutableStateOf(NavigationCameraMode.FOLLOW_3D) }

    val initialLat = navigationState.driverLat ?: navigationState.targetLat ?: DEFAULT_MAP_LAT
    val initialLng = navigationState.driverLng ?: navigationState.targetLng ?: DEFAULT_MAP_LNG

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(LatLng(initialLat, initialLng))
            .zoom(FOLLOW_ZOOM)
            .tilt(FOLLOW_3D_TILT)
            .bearing(navigationState.activeBearing)
            .build()
    }

    LaunchedEffect(cameraPositionState.isMoving, cameraPositionState.cameraMoveStartedReason) {
        if (cameraPositionState.isMoving &&
            cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE
        ) {
            cameraMode = NavigationCameraMode.FREE_PAN
        }
    }

    val effectiveDriverCoord = remember(
        navigationState.driverLat,
        navigationState.driverLng,
        navigationState.remainingPolyline
    ) {
        val firstPoly = navigationState.remainingPolyline.firstOrNull()
        if (firstPoly != null) {
            firstPoly
        } else if (navigationState.driverLat != null && navigationState.driverLng != null) {
            Pair(navigationState.driverLat, navigationState.driverLng)
        } else {
            null
        }
    }

    LaunchedEffect(
        cameraMode,
        effectiveDriverCoord,
        navigationState.activeBearing,
        navigationState.targetLat,
        navigationState.targetLng
    ) {
        when (cameraMode) {
            NavigationCameraMode.FOLLOW_3D -> {
                val coord = effectiveDriverCoord ?: return@LaunchedEffect
                animateToFollow3D(
                    cameraPositionState = cameraPositionState,
                    lat = coord.first,
                    lng = coord.second,
                    bearing = navigationState.activeBearing
                )
            }
            NavigationCameraMode.FOLLOW_2D -> {
                val coord = effectiveDriverCoord ?: return@LaunchedEffect
                animateToFollow2D(
                    cameraPositionState = cameraPositionState,
                    lat = coord.first,
                    lng = coord.second,
                    bearing = navigationState.activeBearing
                )
            }
            NavigationCameraMode.OVERVIEW -> {
                animateToOverview(
                    cameraPositionState = cameraPositionState,
                    navigationState = navigationState
                )
            }
            NavigationCameraMode.FREE_PAN -> Unit
        }
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    }
    val mapProperties = remember {
        MapProperties(isMyLocationEnabled = false, isBuildingEnabled = true)
    }

    val isNavigatingMode = cameraMode == NavigationCameraMode.FOLLOW_3D || cameraMode == NavigationCameraMode.FOLLOW_2D

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties,
            onMapLoaded = {
                if (bikeIcon == null) {
                    bikeIcon = createDriverVehicleIcon(context, R.drawable.marker_omnibike, markerWidth, markerHeight)
                }
                if (car4Icon == null) {
                    car4Icon = createDriverVehicleIcon(context, R.drawable.marker_omnicar4, markerWidth, markerHeight)
                }
                if (car7Icon == null) {
                    car7Icon = createDriverVehicleIcon(context, R.drawable.marker_omnicar7, markerWidth, markerHeight)
                }
            },
            contentPadding = PaddingValues(
                top = Dimen.SizeUltra,
                bottom = bottomSheetPadding
            )
        ) {
            if (navigationState.remainingPolyline.size >= 2) {
                Polyline(
                    points = navigationState.remainingPolyline.map { LatLng(it.first, it.second) },
                    color = PrimaryColor,
                    width = POLYLINE_WIDTH_PX,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    jointType = JointType.ROUND
                )
            }

            if (navigationState.targetLat != null && navigationState.targetLng != null) {
                Marker(
                    state = MarkerState(position = LatLng(navigationState.targetLat, navigationState.targetLng)),
                    title = stringResource(id = R.string.driver_nav_marker_target),
                    snippet = navigationState.targetAddress,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }

            if (navigationState.secondaryLat != null && navigationState.secondaryLng != null) {
                Marker(
                    state = MarkerState(position = LatLng(navigationState.secondaryLat, navigationState.secondaryLng)),
                    title = stringResource(id = R.string.driver_nav_marker_secondary),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }

            effectiveDriverCoord?.let { (lat, lng) ->
                val vehicleIcon = when (navigationState.vehicleType.uppercase()) {
                    "CAR_4_SEAT", "CAR4" -> car4Icon
                    "CAR_7_SEAT", "CAR7" -> car7Icon
                    else -> bikeIcon
                } ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)

                Marker(
                    state = MarkerState(position = LatLng(lat, lng)),
                    title = stringResource(id = R.string.driver_nav_marker_driver),
                    flat = true,
                    rotation = navigationState.activeBearing,
                    anchor = Offset(0.5f, 0.5f),
                    icon = vehicleIcon
                )
            }
        }

        // Floating Action Buttons on Right Side
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = Dimen.PaddingM),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.SPlus)
        ) {
            // Resume Navigation pill when in FREE_PAN or OVERVIEW
            AnimatedVisibility(
                visible = !isNavigatingMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(AppShape.PillShape),
                    colors = CardDefaults.cardColors(containerColor = PrimaryColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXS),
                    modifier = Modifier.clickable {
                        cameraMode = NavigationCameraMode.FOLLOW_3D
                        effectiveDriverCoord?.let { (lat, lng) ->
                            coroutineScope.launch {
                                animateToFollow3D(
                                    cameraPositionState = cameraPositionState,
                                    lat = lat,
                                    lng = lng,
                                    bearing = navigationState.activeBearing
                                )
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = Dimen.PaddingM,
                            vertical = Dimen.PaddingS
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(id = R.string.driver_nav_recenter_3d),
                            tint = OnPrimaryColor,
                            modifier = Modifier.size(Dimen.SizeS2)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XSPlus))
                        Text(
                            text = stringResource(id = R.string.driver_nav_recenter_3d),
                            style = MaterialTheme.typography.s12.bold(),
                            color = OnPrimaryColor
                        )
                    }
                }
            }

            // Overview route button
            Surface(
                onClick = {
                    cameraMode = if (cameraMode == NavigationCameraMode.OVERVIEW) {
                        NavigationCameraMode.FOLLOW_3D
                    } else {
                        NavigationCameraMode.OVERVIEW
                    }
                },
                shape = CircleShape,
                color = SurfaceLight,
                shadowElevation = Dimen.PaddingXS,
                modifier = Modifier.size(Dimen.HeightDefault)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = stringResource(id = R.string.driver_nav_overview),
                        tint = if (cameraMode == NavigationCameraMode.OVERVIEW) PrimaryColor else TextPrimary,
                        modifier = Modifier.size(Dimen.SizeS2)
                    )
                }
            }

            // 3D / 2D Follow-vehicle Navigation Toggle button
            Surface(
                onClick = {
                    cameraMode = if (cameraMode == NavigationCameraMode.FOLLOW_3D) {
                        NavigationCameraMode.FOLLOW_2D
                    } else {
                        NavigationCameraMode.FOLLOW_3D
                    }
                },
                shape = CircleShape,
                color = SurfaceLight,
                shadowElevation = Dimen.PaddingXS,
                modifier = Modifier.size(Dimen.HeightDefault)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (cameraMode == NavigationCameraMode.FOLLOW_2D) {
                            "2D"
                        } else {
                            "3D"
                        },
                        style = MaterialTheme.typography.s12.bold(),
                        color = PrimaryColor
                    )
                }
            }
        }
    }
}
