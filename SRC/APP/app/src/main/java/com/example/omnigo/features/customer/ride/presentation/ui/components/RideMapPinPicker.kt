package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.core.location.util.reverseGeocode
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.features.customer.ride.presentation.state.LocationTarget
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DEFAULT_LATITUDE = 10.776889
private const val DEFAULT_LONGITUDE = 106.700806
private const val PIN_PICKER_ZOOM = 16f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideMapPinPicker(
    target: LocationTarget,
    initialPoint: LocationPoint?,
    onConfirmLocation: (LocationPoint) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentLat by remember { mutableDoubleStateOf(initialPoint?.latitude ?: DEFAULT_LATITUDE) }
    var currentLng by remember { mutableDoubleStateOf(initialPoint?.longitude ?: DEFAULT_LONGITUDE) }
    var locationName by remember { mutableStateOf(initialPoint?.name ?: context.getString(R.string.ride_pin_picker_detecting)) }
    var locationAddress by remember { mutableStateOf(initialPoint?.address ?: "") }
    var isGeocoding by remember { mutableStateOf(false) }
    var geocodeJob by remember { mutableStateOf<Job?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(currentLat, currentLng), PIN_PICKER_ZOOM)
    }

    fun triggerReverseGeocode(lat: Double, lng: Double) {
        geocodeJob?.cancel()
        geocodeJob = coroutineScope.launch {
            delay(350)
            isGeocoding = true
            val (name, address) = reverseGeocode(context, lat, lng)
            locationName = name
            locationAddress = address
            isGeocoding = false
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val center = cameraPositionState.position.target
            currentLat = center.latitude
            currentLng = center.longitude
            triggerReverseGeocode(center.latitude, center.longitude)
        }
    }

    LaunchedEffect(initialPoint) {
        if (initialPoint != null) {
            currentLat = initialPoint.latitude
            currentLng = initialPoint.longitude
            locationName = initialPoint.name
            locationAddress = initialPoint.address
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(initialPoint.latitude, initialPoint.longitude), PIN_PICKER_ZOOM)
        } else {
            triggerReverseGeocode(currentLat, currentLng)
        }
    }

    val pinElevation by animateDpAsState(
        targetValue = if (cameraPositionState.isMoving) Dimen.PaddingM else 0.dp,
        label = "pinElevation"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(AppShape.ShapeXS),
                            color = if (target == LocationTarget.PICKUP) PrimaryColor else SecondaryColor
                        ) {
                            Text(
                                text = stringResource(
                                    id = if (target == LocationTarget.PICKUP) R.string.ride_pin_picker_badge_pickup else R.string.ride_pin_picker_badge_dropoff
                                ),
                                style = MaterialTheme.typography.s10.bold(),
                                color = TextWhite,
                                modifier = Modifier.padding(horizontal = Dimen.PaddingXS, vertical = Dimen.PaddingXXS)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = stringResource(
                                id = if (target == LocationTarget.PICKUP) R.string.ride_pin_picker_title_pickup else R.string.ride_pin_picker_title_dropoff
                            ),
                            style = MaterialTheme.typography.s16.bold(),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Real Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = true,
                    mapToolbarEnabled = false
                )
            )

            // Prominent Target Guidance Card on top of map
            Surface(
                shape = RoundedCornerShape(AppShape.ShapeL),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = Dimen.PaddingS,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = Dimen.PaddingM)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.PaddingS)
                            .clip(CircleShape)
                            .background(if (target == LocationTarget.PICKUP) PrimaryColor else SecondaryColor)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = stringResource(id = R.string.ride_pin_picker_drag_hint),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextPrimary
                    )
                }
            }

            // Fixed Center Pin with shadow & spring elevation
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -Dimen.SizeM - pinElevation),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = if (target == LocationTarget.PICKUP) PrimaryColor else SecondaryColor,
                        modifier = Modifier.size(Dimen.HeightDefault)
                    )
                    // Pin Shadow
                    Box(
                        modifier = Modifier
                            .size(width = Dimen.PaddingSM, height = Dimen.PaddingXS)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    )
                }
            }

            // My Location FAB
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        val targetLat = initialPoint?.latitude ?: DEFAULT_LATITUDE
                        val targetLng = initialPoint?.longitude ?: DEFAULT_LONGITUDE
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(targetLat, targetLng), PIN_PICKER_ZOOM))
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Dimen.PaddingM, bottom = Dimen.HeightPinPickerBottomOffset)
                    .shadow(Dimen.PaddingS, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null)
            }

            // Bottom Confirmation Sheet Card
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = Dimen.PaddingM
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(Dimen.PaddingM)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(Dimen.SizeL)
                                .clip(CircleShape)
                                .background(
                                    if (target == LocationTarget.PICKUP) {
                                        PrimaryColor.copy(alpha = 0.1f)
                                    } else {
                                        SecondaryColor.copy(alpha = 0.1f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (target == LocationTarget.PICKUP) PrimaryColor else SecondaryColor,
                                modifier = Modifier.size(Dimen.SizeM)
                            )
                        }

                        Spacer(modifier = Modifier.width(AppSpacing.M))

                        Column(modifier = Modifier.weight(1f)) {
                            if (isGeocoding) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(Dimen.SizeS),
                                        strokeWidth = Dimen.PaddingXXS,
                                        color = PrimaryColor
                                    )
                                    Spacer(modifier = Modifier.width(AppSpacing.S))
                                    Text(
                                        text = stringResource(id = R.string.ride_pin_picker_detecting),
                                        style = MaterialTheme.typography.s14.medium(),
                                        color = TextSecondary
                                    )
                                }
                            } else {
                                Text(
                                    text = locationName,
                                    style = MaterialTheme.typography.s16.bold(),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (locationAddress.isNotBlank()) {
                                    Text(
                                        text = locationAddress,
                                        style = MaterialTheme.typography.s12.medium(),
                                        color = TextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.M))

                    Button(
                        onClick = {
                            val confirmedPoint = LocationPoint(
                                name = locationName,
                                address = locationAddress.ifBlank { "Lat: $currentLat, Lng: $currentLng" },
                                latitude = currentLat,
                                longitude = currentLng
                            )
                            onConfirmLocation(confirmedPoint)
                        },
                        enabled = !isGeocoding,
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimen.HeightDefault)
                    ) {
                        Text(
                            text = stringResource(
                                id = if (target == LocationTarget.PICKUP) {
                                    R.string.ride_pin_picker_btn_confirm_pickup
                                } else {
                                    R.string.ride_pin_picker_btn_confirm_dropoff
                                }
                            ),
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextWhite
                        )
                    }
                }
            }
        }
    }
}
