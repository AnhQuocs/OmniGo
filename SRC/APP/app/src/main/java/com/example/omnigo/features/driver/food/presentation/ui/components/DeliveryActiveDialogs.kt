package com.example.omnigo.features.driver.food.presentation.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import java.util.Locale

@Composable
fun DeliveryExitConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.driver_delivery_exit_dialog_title),
                style = MaterialTheme.typography.s18.bold()
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.driver_delivery_exit_dialog_msg),
                style = MaterialTheme.typography.s14.normal()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmExit,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) { Text(text = stringResource(id = R.string.driver_delivery_btn_exit)) }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainer,
                    contentColor = TextPrimary
                )
            ) { Text(text = stringResource(id = R.string.driver_delivery_btn_stay)) }
        }
    )
}

@Composable
fun DeliveryCompletedDialog(
    earnedFee: Double,
    onNavigateHome: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
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
                onClick = onNavigateHome,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(text = stringResource(id = R.string.driver_delivery_btn_home))
            }
        }
    )
}

