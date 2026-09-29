package com.example.omnigo.features.customer.food.presentation.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

@Composable
fun RestaurantConflictDialog(
    currentRestaurantName: String,
    newRestaurantName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentName = currentRestaurantName.ifBlank { stringResource(id = R.string.restaurant_conflict_fallback_current) }
    val newName = newRestaurantName.ifBlank { stringResource(id = R.string.restaurant_conflict_fallback_new) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.restaurant_conflict_dialog_title),
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = stringResource(
                    id = R.string.restaurant_conflict_dialog_message,
                    currentName,
                    newName
                ),
                style = MaterialTheme.typography.s14.normal(),
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(AppShape.ShapeXL),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(
                    text = stringResource(id = R.string.restaurant_conflict_dialog_confirm),
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextWhite
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppShape.ShapeXL)
            ) {
                Text(
                    text = stringResource(id = R.string.restaurant_conflict_dialog_cancel),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary
                )
            }
        },
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(AppShape.ShapeL)
    )
}
