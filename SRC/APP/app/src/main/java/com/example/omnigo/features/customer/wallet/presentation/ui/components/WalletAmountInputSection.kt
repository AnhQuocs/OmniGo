package com.example.omnigo.features.customer.wallet.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.presentation.format.formatFoodPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s28

@Composable
fun WalletAmountInputSection(
    amountText: String,
    onAmountChange: (String) -> Unit,
    isMinAmountValid: Boolean,
    isWithdraw: Boolean,
    isBalanceSufficient: Boolean,
    currentBalance: Double?,
    presets: List<Long>,
    enteredAmount: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppShape.ShapeM),
            color = SurfaceVariantLight,
            border = BorderStroke(
                Dimen.BorderThin,
                if (!isMinAmountValid && amountText.isNotEmpty()) ErrorColor else OutlineVariantLight
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₫",
                            style = MaterialTheme.typography.s28.bold(),
                            color = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }
                                if (digitsOnly.length <= 9) {
                                    onAmountChange(digitsOnly)
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.s28.bold().copy(color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent
                            ),
                            placeholder = {
                                Text(
                                    text = "0",
                                    style = MaterialTheme.typography.s28.bold(),
                                    color = TextSecondary.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (amountText.isNotEmpty()) {
                        IconButton(onClick = { onAmountChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(Dimen.SizeS2)
                            )
                        }
                    }
                }

                if (isWithdraw && currentBalance != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.wallet_available_balance, formatFoodPrice(currentBalance)),
                            style = MaterialTheme.typography.s12.medium(),
                            color = TextSecondary
                        )
                        Text(
                            text = stringResource(id = R.string.wallet_withdraw_all),
                            style = MaterialTheme.typography.s12.bold(),
                            color = PrimaryColor,
                            modifier = Modifier.clickable {
                                onAmountChange(currentBalance.toLong().coerceAtLeast(0).toString())
                            }
                        )
                    }
                }
            }
        }

        if (amountText.isNotEmpty()) {
            if (!isMinAmountValid) {
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = stringResource(id = R.string.wallet_amount_min_error),
                    style = MaterialTheme.typography.s12.normal(),
                    color = ErrorColor
                )
            } else if (isWithdraw && !isBalanceSufficient) {
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = stringResource(id = R.string.wallet_withdraw_max_error),
                    style = MaterialTheme.typography.s12.normal(),
                    color = ErrorColor
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
        ) {
            presets.forEach { preset ->
                val isSelected = enteredAmount.toLong() == preset
                Surface(
                    onClick = { onAmountChange(preset.toString()) },
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    color = if (isSelected) PrimaryContainer else SurfaceLight,
                    border = BorderStroke(
                        Dimen.BorderThin,
                        if (isSelected) PrimaryColor else OutlineVariantLight
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = Dimen.PaddingS)
                    ) {
                        Text(
                            text = "${preset / 1000}k",
                            style = if (isSelected) MaterialTheme.typography.s13.bold() else MaterialTheme.typography.s13.medium(),
                            color = if (isSelected) PrimaryColor else TextPrimary
                        )
                    }
                }
            }
        }
    }
}

