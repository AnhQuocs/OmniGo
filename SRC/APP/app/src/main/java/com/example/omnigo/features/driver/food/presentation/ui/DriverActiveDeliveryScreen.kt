package com.example.omnigo.features.driver.food.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import com.example.omnigo.features.driver.food.presentation.ui.components.DeliveryBottomAction
import com.example.omnigo.features.driver.food.presentation.ui.components.DeliveryCashCollectionCard
import com.example.omnigo.features.driver.food.presentation.ui.components.DeliveryDestinationCard
import com.example.omnigo.features.driver.food.presentation.ui.components.DeliveryOrderItemsCard
import com.example.omnigo.features.driver.food.presentation.viewmodel.DriverActiveDeliveryUiState
import com.example.omnigo.features.driver.food.presentation.viewmodel.DriverActiveDeliveryViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverActiveDeliveryScreen(
    orderId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverActiveDeliveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !uiState.isCompleted) {
        showExitDialog = true
    }

    LaunchedEffect(orderId) {
        if (orderId > 0L) {
            viewModel.loadOrderDetail(orderId)
        }
    }

    DisposableEffect(orderId) {
        viewModel.startPeriodicLocationTracking()
        onDispose {
            viewModel.stopPeriodicLocationTracking()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage ?: return@LaunchedEffect
        val msg = if (error == DriverActiveDeliveryViewModel.ERROR_GEOFENCE_OUTSIDE) {
            context.getString(R.string.driver_delivery_error_geofence_outside)
        } else {
            error
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.clearErrorMessage()
    }

    LaunchedEffect(uiState.actionMessage) {
        val action = uiState.actionMessage ?: return@LaunchedEffect
        val msg = when (action) {
            DriverActiveDeliveryViewModel.MESSAGE_PICKUP_SUCCESS -> context.getString(R.string.driver_delivery_pickup_success)
            DriverActiveDeliveryViewModel.MESSAGE_DELIVERY_SUCCESS -> context.getString(R.string.driver_delivery_completed_success)
            else -> action
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.clearActionMessage()
    }

    Scaffold(
        modifier = modifier.fillMaxSize().background(BackgroundLight),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.driver_delivery_title, orderId),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.isCompleted) {
                                onNavigateBack()
                            } else {
                                showExitDialog = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.driver_delivery_btn_back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        bottomBar = {
            DeliveryBottomAction(
                uiState = uiState,
                onConfirmPickup = { viewModel.onConfirmPickup() },
                onConfirmDelivered = { viewModel.onConfirmDelivered() },
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.order == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            val order = uiState.order
            if (order != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
                ) {
                    val isPickup = order.status == DriverDeliveryStatus.ACCEPTED

                    // Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.driver_delivery_status_label),
                            style = MaterialTheme.typography.s13.medium(),
                            color = TextSecondary
                        )

                        val badgeText = if (isPickup) {
                            stringResource(id = R.string.driver_delivery_badge_pickup)
                        } else {
                            stringResource(id = R.string.driver_delivery_badge_delivering)
                        }

                        val badgeColor = if (isPickup) PrimaryColor else SuccessColor
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppShape.ShapeS))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXXS)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.s13.semiBold(),
                                color = badgeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.M))

                    // Destination Card
                    if (isPickup) {
                        DeliveryDestinationCard(
                            isPickupStep = true,
                            name = order.restaurantName,
                            address = order.restaurantAddress,
                            phone = null,
                            distanceMeters = uiState.distanceToTargetMeters,
                            isWithinGeofence = uiState.isWithinGeofence,
                            hasCoordinates = (order.restaurantLatitude != null && order.restaurantLongitude != null)
                        )
                    } else {
                        DeliveryDestinationCard(
                            isPickupStep = false,
                            name = order.customerName,
                            address = order.dropOffAddress,
                            phone = order.customerPhone,
                            distanceMeters = uiState.distanceToTargetMeters,
                            isWithinGeofence = uiState.isWithinGeofence,
                            hasCoordinates = (order.dropOffLatitude != null && order.dropOffLongitude != null)
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.M))

                    // Cash Collection Guidance Card
                    DeliveryCashCollectionCard(
                        isPaid = order.isPaid,
                        cashAmountToCollect = order.cashAmountToCollect
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.M))

                    // Items list
                    if (order.items.isNotEmpty()) {
                        DeliveryOrderItemsCard(
                            items = order.items,
                            note = order.note
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.XL))
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = stringResource(id = R.string.driver_delivery_exit_dialog_title), style = MaterialTheme.typography.s18.bold()) },
            text = { Text(text = stringResource(id = R.string.driver_delivery_exit_dialog_msg), style = MaterialTheme.typography.s14.normal()) },
            confirmButton = {
                Button(
                    onClick = { showExitDialog = false; onNavigateBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text(text = stringResource(id = R.string.driver_delivery_btn_exit))
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExitDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = TextPrimary)
                ) {
                    Text(text = stringResource(id = R.string.driver_delivery_btn_stay))
                }
            }
        )
    }

    if (uiState.isCompleted) {
        val earnedFee = uiState.order?.deliveryFee ?: 0.0
        AlertDialog(
            onDismissRequest = { /* Require button click */ },
            title = {
                Text(
                    text = stringResource(id = R.string.driver_delivery_completed_dialog_title),
                    style = MaterialTheme.typography.s18.bold()
                )
            },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.driver_delivery_completed_dialog_msg,
                        String.format(Locale.US, "%,.0f ₫", earnedFee)
                    ),
                    style = MaterialTheme.typography.s14.normal()
                )
            },
            confirmButton = {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text(text = stringResource(id = R.string.driver_delivery_btn_home))
                }
            }
        )
    }
}

