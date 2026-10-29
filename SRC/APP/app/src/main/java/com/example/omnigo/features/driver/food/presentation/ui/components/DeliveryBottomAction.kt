package com.example.omnigo.features.driver.food.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.common.ui.components.DriverSwipeButton
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import com.example.omnigo.features.driver.food.presentation.viewmodel.DriverActiveDeliveryUiState
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s16

@Composable
fun DeliveryBottomAction(
    uiState: DriverActiveDeliveryUiState,
    onConfirmPickup: () -> Unit,
    onConfirmDelivered: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val order = uiState.order ?: return
    val isPickup = order.status == DriverDeliveryStatus.ACCEPTED
    val isCompleted = order.status == DriverDeliveryStatus.COMPLETED

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceLight)
            .navigationBarsPadding()
            .padding(vertical = Dimen.PaddingSM)
    ) {
        when {
            isCompleted -> {
                Button(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_delivery_btn_home),
                        style = MaterialTheme.typography.s16.bold(),
                        color = OnPrimaryColor
                    )
                }
            }
            isPickup -> {
                DriverSwipeButton(
                    text = stringResource(id = R.string.driver_delivery_btn_confirm_pickup),
                    onSwipeConfirm = onConfirmPickup,
                    isLoading = uiState.isUpdatingStatus,
                    enabled = !uiState.isUpdatingStatus,
                    trackColor = if (uiState.isWithinGeofence) SuccessColor else PrimaryColor,
                    thumbColor = OnPrimaryColor,
                    contentColor = OnPrimaryColor
                )
            }
            else -> {
                DriverSwipeButton(
                    text = stringResource(id = R.string.driver_delivery_btn_confirm_delivered),
                    onSwipeConfirm = onConfirmDelivered,
                    isLoading = uiState.isUpdatingStatus,
                    enabled = !uiState.isUpdatingStatus,
                    trackColor = SuccessColor,
                    thumbColor = OnPrimaryColor,
                    contentColor = OnPrimaryColor
                )
            }
        }
    }
}
