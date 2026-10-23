package com.example.omnigo.features.driver.food.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import com.example.omnigo.features.driver.food.presentation.viewmodel.DriverActiveDeliveryUiState
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
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
            .background(BackgroundLight)
            .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
    ) {
        when {
            isCompleted -> {
                Button(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    modifier = Modifier.fillMaxWidth().height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_delivery_btn_home),
                        style = MaterialTheme.typography.s16.bold(),
                        color = OnPrimaryColor
                    )
                }
            }
            isPickup -> {
                Button(
                    onClick = onConfirmPickup,
                    enabled = uiState.isWithinGeofence && !uiState.isUpdatingStatus,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        contentColor = OnPrimaryColor
                    ),
                    modifier = Modifier.fillMaxWidth().height(Dimen.HeightDefault)
                ) {
                    if (uiState.isUpdatingStatus) {
                        CircularProgressIndicator(
                            color = OnPrimaryColor,
                            modifier = Modifier.size(Dimen.SizeM),
                            strokeWidth = Dimen.PaddingXXS
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.driver_delivery_btn_confirm_pickup),
                            style = MaterialTheme.typography.s16.bold()
                        )
                    }
                }
            }
            else -> {
                Button(
                    onClick = onConfirmDelivered,
                    enabled = uiState.isWithinGeofence && !uiState.isUpdatingStatus,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessColor,
                        contentColor = OnPrimaryColor
                    ),
                    modifier = Modifier.fillMaxWidth().height(Dimen.HeightDefault)
                ) {
                    if (uiState.isUpdatingStatus) {
                        CircularProgressIndicator(
                            color = OnPrimaryColor,
                            modifier = Modifier.size(Dimen.SizeM),
                            strokeWidth = Dimen.PaddingXXS
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.driver_delivery_btn_confirm_delivered),
                            style = MaterialTheme.typography.s16.bold()
                        )
                    }
                }
            }
        }
    }
}
