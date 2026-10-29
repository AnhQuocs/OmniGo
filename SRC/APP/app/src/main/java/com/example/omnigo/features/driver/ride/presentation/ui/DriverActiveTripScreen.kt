package com.example.omnigo.features.driver.ride.presentation.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.driver.common.ui.components.DriverCashPaymentBanner
import com.example.omnigo.features.driver.common.ui.components.DriverGeofenceOverrideSheet
import com.example.omnigo.features.driver.common.ui.components.DriverInAppNavigationMap
import com.example.omnigo.features.driver.common.ui.components.DriverNavigationHudBanner
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripActionSection
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripCancelDialog
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripCustomerCard
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripGeofencePill
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripRouteCard
import com.example.omnigo.features.driver.ride.presentation.ui.components.DriverTripSettlementDialog
import com.example.omnigo.features.driver.ride.presentation.viewmodel.DriverActiveTripViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s12

@Composable
fun DriverActiveTripScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverActiveTripViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isSheetExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (uiState.isCompleted || uiState.isTripCancelled) {
            onNavigateBack()
        } else {
            viewModel.showCancelConfirmDialog(true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startPeriodicLocationTracking()
    }

    LaunchedEffect(uiState.isTripCancelled) {
        if (uiState.isTripCancelled) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage
        if (error != null) {
            val message = when (error) {
                DriverActiveTripViewModel.GEOFENCE_WARNING_PICKUP ->
                    context.getString(R.string.driver_trip_geofence_warning_pickup)
                DriverActiveTripViewModel.GEOFENCE_WARNING_DROPOFF ->
                    context.getString(R.string.driver_trip_geofence_warning_dropoff)
                else -> error
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    val stageLabel = when (uiState.status) {
        BookingStatus.ACCEPTED -> stringResource(id = R.string.driver_nav_heading_to_pickup)
        BookingStatus.ARRIVED -> stringResource(id = R.string.driver_trip_status_arrived)
        BookingStatus.IN_PROGRESS -> stringResource(id = R.string.driver_nav_heading_to_dropoff)
        BookingStatus.COMPLETED -> stringResource(id = R.string.driver_trip_status_completed)
        else -> stringResource(id = R.string.driver_trip_title)
    }

    val canCancelTrip = uiState.status == BookingStatus.ACCEPTED || uiState.status == BookingStatus.ARRIVED

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // 1. Full-Screen 3D Navigation Map
        DriverInAppNavigationMap(
            navigationState = navigationState,
            bottomSheetPadding = if (isSheetExpanded) {
                Dimen.HeightRideBottomSheetOffset
            } else {
                Dimen.HeightPinPickerBottomOffset
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Turn-by-Turn HUD Banner
        DriverNavigationHudBanner(
            codeNumber = uiState.bookingId,
            stageLabel = stageLabel,
            navigationState = navigationState,
            onBackClick = {
                if (uiState.isCompleted || uiState.isTripCancelled) {
                    onNavigateBack()
                } else {
                    viewModel.showCancelConfirmDialog(true)
                }
            },
            onCancelClick = if (canCancelTrip) {
                { viewModel.showCancelConfirmDialog(true) }
            } else {
                null
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 3. Bottom Collapsible Cockpit Sheet
        Card(
            shape = RoundedCornerShape(
                topStart = AppShape.ShapeXL2,
                topEnd = AppShape.ShapeXL2
            ),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingS),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = Dimen.PaddingS)
                        .size(width = Dimen.ShimmerTextWidthXXS, height = Dimen.PaddingXS)
                        .clip(CircleShape)
                        .background(OutlineLight)
                )

                // Sheet Handle & Expand/Collapse Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSheetExpanded = !isSheetExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DriverTripGeofencePill(
                        distanceToTargetMeters = uiState.distanceToTargetMeters,
                        isWithinGeofence = uiState.isWithinGeofence,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.S))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppShape.PillShape))
                            .background(PrimaryContainer)
                            .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(
                                id = if (isSheetExpanded) {
                                    R.string.driver_nav_sheet_collapse
                                } else {
                                    R.string.driver_nav_sheet_expand
                                }
                            ),
                            style = MaterialTheme.typography.s12.bold(),
                            color = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Icon(
                            imageVector = if (isSheetExpanded) {
                                Icons.Default.KeyboardArrowDown
                            } else {
                                Icons.Default.KeyboardArrowUp
                            },
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeS2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Primary Swipe Action Button
                DriverTripActionSection(
                    status = uiState.status,
                    isWithinGeofence = uiState.isWithinGeofence,
                    isUpdatingStatus = uiState.isUpdatingStatus,
                    onArrivePickup = viewModel::onArrivePickup,
                    onStartTrip = viewModel::onStartTrip,
                    onCompleteTrip = viewModel::onCompleteTrip,
                    onRequestCancel = { viewModel.showCancelConfirmDialog(true) }
                )

                // Expandable Details Section
                AnimatedVisibility(visible = isSheetExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.SPlus)
                    ) {
                        Spacer(modifier = Modifier.height(AppSpacing.XS))

                        if (uiState.isCashPayment) {
                            DriverCashPaymentBanner(
                                isCash = uiState.isCashPayment,
                                amountToCollect = uiState.amountToCollect
                            )
                        }

                        DriverTripCustomerCard(
                            customerId = uiState.customerId,
                            customerName = uiState.customerName,
                            customerPhone = uiState.customerPhone,
                            customerAvatarUrl = uiState.customerAvatarUrl
                        )

                        DriverTripRouteCard(
                            pickupAddress = uiState.pickupAddress,
                            dropoffAddress = uiState.dropoffAddress,
                            startLat = uiState.startLatitude,
                            startLng = uiState.startLongitude,
                            endLat = uiState.endLatitude,
                            endLng = uiState.endLongitude
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.XS))
                    }
                }
            }
        }
    }

    if (uiState.showGeofenceOverrideDialog) {
        DriverGeofenceOverrideSheet(
            distanceMeters = uiState.distanceToTargetMeters,
            onConfirmOverride = viewModel::onConfirmGeofenceOverride,
            onDismiss = viewModel::dismissGeofenceOverrideDialog
        )
    }

    if (uiState.showCancelConfirmDialog) {
        DriverTripCancelDialog(
            onConfirm = viewModel::onConfirmCancelTrip,
            onDismiss = { viewModel.showCancelConfirmDialog(false) }
        )
    }

    if (uiState.showSettlementDialog) {
        DriverTripSettlementDialog(
            totalFare = uiState.price,
            platformFee = uiState.platformFee,
            driverEarnings = uiState.driverEarnings,
            isCashPayment = uiState.isCashPayment,
            onDismissAndFinish = {
                viewModel.dismissSettlementDialog()
                onNavigateBack()
            }
        )
    }
}
