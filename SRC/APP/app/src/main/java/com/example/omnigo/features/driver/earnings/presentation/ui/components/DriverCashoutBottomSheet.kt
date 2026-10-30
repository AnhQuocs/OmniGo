package com.example.omnigo.features.driver.earnings.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverCashoutBottomSheet(
    availableBalance: Double,
    isSubmitting: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmCashout: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val minAmount = 50_000.0
    val errMin = stringResource(id = R.string.driver_cashout_error_min)
    val errExceed = stringResource(id = R.string.driver_cashout_error_exceed)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
        ) {
            Text(
                text = stringResource(id = R.string.driver_cashout_sheet_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Available Balance Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppShape.ShapeL))
                    .background(PrimaryContainer)
                    .padding(Dimen.PaddingM)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.driver_cashout_available_balance),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = String.format(Locale.getDefault(), "%,.0f ₫", availableBalance),
                            style = MaterialTheme.typography.s20.bold(),
                            color = PrimaryColor
                        )
                    }

                    // Button "All" (Tất cả)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppShape.PillShape))
                            .background(PrimaryColor)
                            .clickable(enabled = !isSubmitting) {
                                if (availableBalance > 0) {
                                    amountInput = String.format(Locale.US, "%.0f", availableBalance)
                                    errorMessage = null
                                }
                            }
                            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXS),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.driver_cashout_btn_all),
                            style = MaterialTheme.typography.s12.bold(),
                            color = OnPrimaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Amount Input Field
            Text(
                text = stringResource(id = R.string.driver_cashout_amount_label),
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            OutlinedTextField(
                value = amountInput,
                onValueChange = {
                    amountInput = it.filter { ch -> ch.isDigit() }
                    errorMessage = null
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.driver_cashout_amount_hint),
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    Text(
                        text = "₫",
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextSecondary,
                        modifier = Modifier.padding(end = Dimen.PaddingM)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !isSubmitting,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = CardBorderColor
                ),
                shape = RoundedCornerShape(AppShape.ShapeM),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.s12.normal(),
                    color = ErrorColor
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Bank Account Info Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppShape.ShapeL))
                    .border(Dimen.BorderHairline, CardBorderColor, RoundedCornerShape(AppShape.ShapeL))
                    .padding(Dimen.PaddingSM)
            ) {
                Text(
                    text = stringResource(id = R.string.driver_cashout_bank_info),
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = stringResource(id = R.string.driver_cashout_bank_default),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // Submit Button
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    when {
                        amount < minAmount -> {
                            errorMessage = errMin
                        }
                        amount > availableBalance -> {
                            errorMessage = errExceed
                        }
                        else -> {
                            onConfirmCashout(amount)
                        }
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(AppShape.PillShape),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = OnPrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM),
                        strokeWidth = AppSpacing.XXS
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.driver_cashout_btn_confirm),
                        style = MaterialTheme.typography.s14.bold(),
                        color = OnPrimaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
        }
    }
}
