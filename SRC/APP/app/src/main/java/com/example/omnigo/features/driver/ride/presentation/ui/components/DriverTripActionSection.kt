package com.example.omnigo.features.driver.ride.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.driver.common.ui.components.DriverSwipeButton
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor

@Composable
fun DriverTripActionSection(
    status: BookingStatus,
    isWithinGeofence: Boolean,
    isUpdatingStatus: Boolean,
    onArrivePickup: () -> Unit,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit,
    modifier: Modifier = Modifier,
    onRequestCancel: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            BookingStatus.ACCEPTED -> {
                DriverSwipeButton(
                    text = stringResource(R.string.driver_trip_btn_arrived),
                    onSwipeConfirm = onArrivePickup,
                    isLoading = isUpdatingStatus,
                    enabled = !isUpdatingStatus,
                    trackColor = if (isWithinGeofence) SuccessColor else PrimaryColor,
                    thumbColor = OnPrimaryColor,
                    contentColor = OnPrimaryColor
                )
            }

            BookingStatus.ARRIVED -> {
                DriverSwipeButton(
                    text = stringResource(R.string.driver_trip_btn_start),
                    onSwipeConfirm = onStartTrip,
                    isLoading = isUpdatingStatus,
                    enabled = !isUpdatingStatus,
                    trackColor = PrimaryColor,
                    thumbColor = OnPrimaryColor,
                    contentColor = OnPrimaryColor
                )
            }

            BookingStatus.IN_PROGRESS -> {
                DriverSwipeButton(
                    text = stringResource(R.string.driver_trip_btn_complete),
                    onSwipeConfirm = onCompleteTrip,
                    isLoading = isUpdatingStatus,
                    enabled = !isUpdatingStatus,
                    trackColor = if (isWithinGeofence) SuccessColor else PrimaryColor,
                    thumbColor = OnPrimaryColor,
                    contentColor = OnPrimaryColor
                )
            }

            else -> { /* No action needed */ }
        }
    }
}
