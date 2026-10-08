package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18

private data class CancelReasonOption(
    val code: String,
    val labelRes: Int
)

private val CANCEL_REASONS = listOf(
    CancelReasonOption("CHANGED_MIND", R.string.order_cancel_reason_changed_mind),
    CancelReasonOption("LONG_WAIT", R.string.order_cancel_reason_long_wait),
    CancelReasonOption("WRONG_INFO", R.string.order_cancel_reason_wrong_info),
    CancelReasonOption("OTHER", R.string.order_cancel_reason_other)
)

@Composable
fun CancelOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (reasonCode: String, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCode by remember { mutableStateOf(CANCEL_REASONS.first().code) }
    var customReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.order_cancel_dialog_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.order_cancel_dialog_subtitle),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

                CANCEL_REASONS.forEach { option ->
                    val isSelected = selectedCode == option.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCode = option.code }
                            .padding(vertical = Dimen.PaddingXS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedCode = option.code },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                        )
                        Text(
                            text = stringResource(option.labelRes),
                            style = MaterialTheme.typography.s14.normal(),
                            color = TextPrimary,
                            modifier = Modifier.padding(start = Dimen.PaddingS)
                        )
                    }
                }

                if (selectedCode == "OTHER") {
                    Spacer(modifier = Modifier.height(AppSpacing.S))
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.order_cancel_reason_other_hint),
                                style = MaterialTheme.typography.s14.normal()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.ShapeS)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (selectedCode == "OTHER" && customReason.isNotBlank()) {
                        customReason.trim()
                    } else {
                        selectedCode
                    }
                    onConfirm(selectedCode, finalReason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(AppShape.ShapeM)
            ) {
                Text(
                    text = stringResource(R.string.order_cancel_confirm),
                    color = TextWhite,
                    style = MaterialTheme.typography.s14.bold()
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.order_cancel_dismiss),
                    color = TextSecondary,
                    style = MaterialTheme.typography.s14.bold()
                )
            }
        },
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(AppShape.ShapeL),
        modifier = modifier
    )
}
