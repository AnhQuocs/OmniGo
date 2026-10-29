package com.example.omnigo.features.driver.food.presentation.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.omnigo.features.driver.common.ui.components.DriverCashPaymentBanner
import com.example.omnigo.features.driver.common.ui.components.DriverGeofenceOverrideSheet
import com.example.omnigo.features.driver.common.ui.components.DriverInAppNavigationMap
import com.example.omnigo.features.driver.common.ui.components.DriverNavigationHudBanner
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import com.example.omnigo.features.driver.food.presentation.ui.components.DeliveryBottomAction
import com.example.omnigo.features.driver.food.presentation.ui.components.DeliveryCompactHeaderAndStepper
import com.example.omnigo.features.driver.food.presentation.ui.components.DeliveryCompletedDialog
import com.example.omnigo.features.driver.food.presentation.ui.components.DeliveryDestinationCard
import com.example.omnigo.features.driver.food.presentation.ui.components.DeliveryExitConfirmDialog
import com.example.omnigo.features.driver.food.presentation.ui.components.DeliveryOrderItemsCard
import com.example.omnigo.features.driver.food.presentation.viewmodel.DriverActiveDeliveryViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight

@Composable
fun DriverActiveDeliveryScreen(
    orderId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverActiveDeliveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showExitDialog by remember { mutableStateOf(false) }
    var isSheetExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = !uiState.isCompleted) { showExitDialog = true }

    LaunchedEffect(orderId) {
        if (orderId > 0L) viewModel.loadOrderDetail(orderId)
    }

    DisposableEffect(orderId) {
        viewModel.startPeriodicLocationTracking()
        onDispose { viewModel.stopPeriodicLocationTracking() }
    }

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage ?: return@LaunchedEffect
        val msg = if (error == DriverActiveDeliveryViewModel.ERROR_GEOFENCE_OUTSIDE) {
            context.getString(R.string.driver_delivery_error_geofence_outside)
        } else error
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.clearErrorMessage()
    }

    LaunchedEffect(uiState.actionMessage) {
        val action = uiState.actionMessage ?: return@LaunchedEffect
        val msg = when (action) {
            DriverActiveDeliveryViewModel.MESSAGE_PICKUP_SUCCESS ->
                context.getString(R.string.driver_delivery_pickup_success)
            DriverActiveDeliveryViewModel.MESSAGE_DELIVERY_SUCCESS ->
                context.getString(R.string.driver_delivery_completed_success)
            else -> action
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.clearActionMessage()
    }

    val order = uiState.order
    val isPickupStep = order?.status != DriverDeliveryStatus.DELIVERING && order?.status != DriverDeliveryStatus.COMPLETED
    val stageLabel = if (isPickupStep) {
        stringResource(id = R.string.driver_nav_heading_to_restaurant)
    } else {
        stringResource(id = R.string.driver_nav_heading_to_customer)
    }

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

        // 2. Top Turn-by-Turn Navigation HUD Banner
        DriverNavigationHudBanner(
            codeNumber = order?.orderId ?: orderId,
            stageLabel = stageLabel,
            navigationState = navigationState,
            onBackClick = {
                if (uiState.isCompleted) onNavigateBack() else showExitDialog = true
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 3. Loading indicator or Bottom Collapsible Cockpit Sheet
        if (uiState.isLoading && order == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(AppShape.ShapeL))
                    .background(SurfaceLight)
                    .padding(Dimen.PaddingL),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else if (order != null) {
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
                        .verticalScroll(scrollState)
                        .padding(
                            start = Dimen.PaddingM,
                            end = Dimen.PaddingM,
                            top = Dimen.PaddingSM
                        )
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

                    DeliveryCompactHeaderAndStepper(
                        orderId = order.orderId,
                        isPickupStep = isPickupStep,
                        isExpanded = isSheetExpanded,
                        onToggleExpand = { isSheetExpanded = !isSheetExpanded }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.SPlus))

                    DriverCashPaymentBanner(
                        isCash = !order.isPaid,
                        amountToCollect = order.cashAmountToCollect
                    )

                    AnimatedVisibility(visible = isSheetExpanded) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(AppSpacing.M))
                            if (isPickupStep) {
                                DeliveryDestinationCard(
                                    isPickupStep = true,
                                    name = order.restaurantName,
                                    address = order.restaurantAddress,
                                    phone = null,
                                    distanceMeters = uiState.distanceToTargetMeters,
                                    isWithinGeofence = uiState.isWithinGeofence,
                                    hasCoordinates = (order.restaurantLatitude != null && order.restaurantLongitude != null),
                                    latitude = order.restaurantLatitude,
                                    longitude = order.restaurantLongitude
                                )
                            } else {
                                DeliveryDestinationCard(
                                    isPickupStep = false,
                                    name = order.customerName,
                                    address = order.dropOffAddress,
                                    phone = order.customerPhone,
                                    distanceMeters = uiState.distanceToTargetMeters,
                                    isWithinGeofence = uiState.isWithinGeofence,
                                    hasCoordinates = (order.dropOffLatitude != null && order.dropOffLongitude != null),
                                    latitude = order.dropOffLatitude,
                                    longitude = order.dropOffLongitude
                                )
                            }

                            if (order.items.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(AppSpacing.M))
                                DeliveryOrderItemsCard(items = order.items, note = order.note)
                            }
                        }
                    }

                    DeliveryBottomAction(
                        uiState = uiState,
                        onConfirmPickup = { viewModel.onConfirmPickup() },
                        onConfirmDelivered = { viewModel.onConfirmDelivered() },
                        onNavigateBack = onNavigateBack
                    )
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

    if (showExitDialog) {
        DeliveryExitConfirmDialog(
            onDismiss = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                onNavigateBack()
            }
        )
    }

    if (uiState.isCompleted) {
        DeliveryCompletedDialog(
            earnedFee = uiState.order?.deliveryFee ?: 0.0,
            onNavigateHome = onNavigateBack
        )
    }
}
