package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18

@Composable
fun CancelTripDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.ride_cancel_active_trip_dialog_title),
                style = MaterialTheme.typography.s18.bold()
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.ride_cancel_active_trip_dialog_message),
                style = MaterialTheme.typography.s14.normal(),
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
            ) {
                Text(text = stringResource(id = R.string.ride_cancel_confirm_yes), color = OnPrimaryColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.ride_cancel_confirm_no), color = TextPrimary)
            }
        }
    )
}

