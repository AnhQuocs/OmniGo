package com.example.omnigo.features.driver.home.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.common.ui.components.DriverSwipeButton
import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OnSurfaceVariantLight
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale
import kotlinx.coroutines.launch

private const val DEFAULT_DRIVER_LAT = 10.776889
private const val DEFAULT_DRIVER_LNG = 106.700806
private const val DEFAULT_DRIVER_ZOOM = 16f

@Composable
fun DriverCockpitMapBackground(
    currentLatitude: Double?,
    currentLongitude: Double?,
    isOnline: Boolean,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetLat = currentLatitude ?: DEFAULT_DRIVER_LAT
    val targetLng = currentLongitude ?: DEFAULT_DRIVER_LNG
    val coroutineScope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(targetLat, targetLng), DEFAULT_DRIVER_ZOOM)
    }

    LaunchedEffect(targetLat, targetLng) {
        runCatching {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(targetLat, targetLng), DEFAULT_DRIVER_ZOOM))
        }
    }

    val uiSettings = remember {
        MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, compassEnabled = true, mapToolbarEnabled = false)
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
            Marker(
                state = MarkerState(position = LatLng(targetLat, targetLng)),
                title = stringResource(id = R.string.driver_status_online),
                icon = BitmapDescriptorFactory.defaultMarker(
                    if (isOnline) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_AZURE
                )
            )
        }

        FloatingActionButton(
            onClick = {
                onRefreshLocation()
                coroutineScope.launch {
                    runCatching {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(targetLat, targetLng), DEFAULT_DRIVER_ZOOM))
                    }
                }
            },
            containerColor = SurfaceLight,
            contentColor = PrimaryColor,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = Dimen.PaddingM).size(Dimen.HeightDefault)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = stringResource(id = R.string.driver_cockpit_recenter_gps),
                modifier = Modifier.size(Dimen.SizeM)
            )
        }
    }
}

@Composable
fun DriverCockpitTopBar(
    profile: DriverAccountProfile?,
    approvalStatus: DriverApprovalStatus,
    dailyReport: DriverDailyReport?,
    modifier: Modifier = Modifier
) {
    val badgeBg = when (approvalStatus) {
        DriverApprovalStatus.APPROVED -> SuccessColor.copy(alpha = 0.14f)
        DriverApprovalStatus.PENDING_APPROVAL -> SecondaryContainer
        DriverApprovalStatus.REJECTED, DriverApprovalStatus.INACTIVE -> ErrorContainer
    }
    val badgeColor = when (approvalStatus) {
        DriverApprovalStatus.APPROVED -> SuccessColor
        DriverApprovalStatus.PENDING_APPROVAL -> WarningColor
        DriverApprovalStatus.REJECTED, DriverApprovalStatus.INACTIVE -> ErrorColor
    }
    val badgeLabelRes = when (approvalStatus) {
        DriverApprovalStatus.APPROVED -> R.string.driver_approval_badge_approved
        DriverApprovalStatus.PENDING_APPROVAL -> R.string.driver_approval_badge_pending
        DriverApprovalStatus.REJECTED, DriverApprovalStatus.INACTIVE -> R.string.driver_approval_badge_rejected
    }

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXS),
        modifier = modifier.fillMaxWidth().border(Dimen.PaddingXXS, CardBorderColor, RoundedCornerShape(AppShape.ShapeL))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.SPlus),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(Dimen.SizeXLPlus).clip(CircleShape).background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(Dimen.SizeM))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile?.fullName?.ifBlank { null } ?: stringResource(id = R.string.ride_driver_name_fallback),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(AppShape.ShapeS)).background(badgeBg)
                            .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                    ) {
                        Text(text = stringResource(id = badgeLabelRes), style = MaterialTheme.typography.s10.semiBold(), color = badgeColor)
                    }
                }
            }
            Spacer(modifier = Modifier.width(AppSpacing.S))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = stringResource(id = R.string.driver_today_earnings), style = MaterialTheme.typography.s10.normal(), color = TextSecondary)
                Text(
                    text = String.format(Locale.getDefault(), "%,.0f ₫", dailyReport?.driverEarnings ?: 0.0),
                    style = MaterialTheme.typography.s16.bold(),
                    color = PrimaryColor
                )
                Text(
                    text = stringResource(id = R.string.driver_cockpit_today_trips, dailyReport?.totalTrips ?: 0),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun DriverCockpitBottomPanel(
    isOnline: Boolean,
    isUpdating: Boolean,
    activeRideBooking: Booking?,
    activeFoodOrder: DriverDeliveryOrder?,
    onToggleStatus: (Boolean) -> Unit,
    onResumeRide: (Booking) -> Unit,
    onResumeFoodOrder: (DriverDeliveryOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = if (isOnline) SuccessColor else OnSurfaceVariantLight,
        animationSpec = tween(durationMillis = 300),
        label = "cockpitStatusColor"
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.SPlus)) {
        activeRideBooking?.let { booking ->
            ActiveTaskBannerCard(
                label = stringResource(id = R.string.driver_cockpit_active_ride_banner, booking.bookingId),
                actionText = stringResource(id = R.string.driver_trips_btn_resume_trip),
                onClick = { onResumeRide(booking) }
            )
        }
        activeFoodOrder?.let { order ->
            ActiveTaskBannerCard(
                label = stringResource(id = R.string.driver_cockpit_active_food_banner, order.orderId),
                actionText = stringResource(id = R.string.driver_trips_btn_resume_food),
                onClick = { onResumeFoodOrder(order) }
            )
        }

        Card(
            shape = RoundedCornerShape(AppShape.ShapeXL),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingS),
            modifier = Modifier.fillMaxWidth().border(
                width = Dimen.PaddingXXS,
                color = if (isOnline) SuccessColor.copy(alpha = 0.45f) else CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeXL)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(Dimen.PaddingM), verticalArrangement = Arrangement.spacedBy(AppSpacing.M)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.SPlus),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.size(Dimen.PaddingSM).clip(CircleShape).background(statusColor))
                        Column {
                            Text(
                                text = stringResource(id = if (isOnline) R.string.driver_status_online else R.string.driver_status_offline),
                                style = MaterialTheme.typography.s18.bold(),
                                color = if (isOnline) SuccessColor else TextPrimary
                            )
                            Text(
                                text = stringResource(id = if (isOnline) R.string.driver_scanning_orders else R.string.driver_status_offline_hint),
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isOnline,
                        enabled = !isUpdating,
                        onCheckedChange = onToggleStatus,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnPrimaryColor,
                            checkedTrackColor = SuccessColor,
                            uncheckedThumbColor = SurfaceLight,
                            uncheckedTrackColor = OutlineVariantLight
                        )
                    )
                }

                DriverSwipeButton(
                    text = stringResource(id = if (isOnline) R.string.driver_swipe_to_offline else R.string.driver_swipe_to_online),
                    onSwipeConfirm = { onToggleStatus(!isOnline) },
                    isLoading = isUpdating,
                    trackColor = if (isOnline) SurfaceVariantLight else SuccessColor,
                    thumbColor = if (isOnline) ErrorColor else OnPrimaryColor,
                    contentColor = if (isOnline) TextPrimary else OnPrimaryColor
                )
            }
        }
    }
}

@Composable
private fun ActiveTaskBannerCard(label: String, actionText: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SecondaryColor),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.s13.bold(), color = OnPrimaryColor, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(AppSpacing.S))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(AppShape.ShapeM)).background(SurfaceLight)
                    .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS)
            ) {
                Text(text = actionText, style = MaterialTheme.typography.s12.bold(), color = SecondaryColor)
            }
        }
    }
}
