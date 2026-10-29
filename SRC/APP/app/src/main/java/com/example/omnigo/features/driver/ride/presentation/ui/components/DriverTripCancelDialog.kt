package com.example.omnigo.features.driver.ride.presentation.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18

@Composable
fun DriverTripCancelDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppShape.ShapeXL),
        containerColor = SurfaceLight,
        title = {
            Text(
                text = stringResource(R.string.driver_trip_cancel_dialog_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = stringResource(R.string.driver_trip_cancel_dialog_message),
                style = MaterialTheme.typography.s14.normal(),
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorColor,
                    contentColor = OnPrimaryColor
                )
            ) {
                Text(
                    text = stringResource(R.string.driver_trip_cancel_confirm),
                    style = MaterialTheme.typography.s14.bold()
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppShape.ShapeM)
            ) {
                Text(
                    text = stringResource(R.string.driver_trip_cancel_dismiss),
                    style = MaterialTheme.typography.s14.bold()
                )
            }
        }
    )
}

