package com.example.omnigo.features.customer.activity.presentation.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.core.components.OmniGoMainLayout
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityEmptyStateSection
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityHeaderSection
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityHistoryListSection
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityOngoingListSection
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityServiceFilterRow
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityTabsSection
import com.example.omnigo.features.customer.activity.presentation.viewmodel.ActivityTab
import com.example.omnigo.features.customer.activity.presentation.viewmodel.ActivityViewModel
import com.example.omnigo.features.customer.food.presentation.ui.activity.FoodActivity
import com.example.omnigo.features.customer.ride.presentation.ui.activity.TripMapActivity
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary

private val TOP_BAR_HEIGHT = 60.dp

@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.loadActivities(isSilent = uiState.hasOngoing || uiState.hasHistory)
        onPauseOrDispose { }
    }

    OmniGoMainLayout(
        modifier = modifier,
        headerHeight = TOP_BAR_HEIGHT,
        header = {
            ActivityHeaderSection()
        }
    ) { contentModifier ->
        Column(
            modifier = contentModifier.fillMaxSize()
        ) {
            ActivityTabsSection(
                selectedTab = uiState.selectedTab,
                ongoingCount = uiState.ongoingCount,
                onTabSelected = viewModel::onTabSelected
            )

            val isHistory = uiState.selectedTab == ActivityTab.HISTORY

            if (isHistory) {
                ActivityServiceFilterRow(
                    selectedService = uiState.selectedHistoryService,
                    onServiceSelected = viewModel::onHistoryServiceSelected
                )
            }

            val isEmpty = if (isHistory) !uiState.hasHistory else !uiState.hasOngoing

            if (uiState.isLoading && isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimen.PaddingXL),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SecondaryColor)
                }
            } else if (isEmpty) {
                ActivityEmptyStateSection(
                    icon = if (isHistory) Icons.Filled.History else Icons.Filled.PendingActions,
                    message = if (isHistory) {
                        stringResource(id = R.string.activity_empty_history)
                    } else {
                        stringResource(id = R.string.activity_empty_ongoing)
                    }
                )
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    if (isHistory) {
                        ActivityHistoryListSection(
                            selectedService = uiState.selectedHistoryService,
                            historyFoodOrders = uiState.historyFoodOrders,
                            historyBookings = uiState.historyBookings,
                            onFoodOrderClick = { order ->
                                context.startActivity(
                                    FoodActivity.createOrderDetailIntent(context, order.id)
                                )
                            },
                            onRideBookingClick = { }
                        )
                    } else {
                        ActivityOngoingListSection(
                            ongoingFoodOrders = uiState.ongoingFoodOrders,
                            ongoingBookings = uiState.ongoingBookings,
                            driverProfiles = uiState.driverProfiles,
                            activeRideState = uiState.activeRideState,
                            onFoodOrderClick = { order ->
                                context.startActivity(
                                    FoodActivity.createOrderDetailIntent(context, order.id)
                                )
                            },
                            onRideBookingClick = { booking ->
                                context.startActivity(
                                    TripMapActivity.createIntent(context, booking.bookingId)
                                )
                            },
                            onCallDriver = { phone -> launchDialer(context, phone) },
                            onMessageDriver = { phone -> launchSms(context, phone) },
                            onCancelRideClick = { booking ->
                                viewModel.promptCancelBooking(booking.bookingId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (uiState.cancelConfirmBookingId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCancelBookingDialog,
            title = {
                Text(
                    text = stringResource(id = R.string.ride_cancel_active_trip_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.ride_cancel_active_trip_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmCancelBooking,
                    enabled = !uiState.isCancellingBooking,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text(text = stringResource(id = R.string.driver_trip_cancel_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::dismissCancelBookingDialog,
                    enabled = !uiState.isCancellingBooking
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_trip_cancel_dismiss),
                        color = TextSecondary
                    )
                }
            }
        )
    }
}

private fun launchDialer(context: Context, phone: String?) {
    if (phone.isNullOrBlank()) return
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

private fun launchSms(context: Context, phone: String?) {
    if (phone.isNullOrBlank()) return
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phone")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
