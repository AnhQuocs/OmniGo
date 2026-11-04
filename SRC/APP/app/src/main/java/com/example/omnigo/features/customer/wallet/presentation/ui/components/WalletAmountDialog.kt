package com.example.omnigo.features.customer.wallet.presentation.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletAmountDialog(
    title: String,
    showMethodSelector: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit,
    currentBalance: Double? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountText by remember { mutableStateOf("50000") }
    val presets = listOf(50_000L, 100_000L, 200_000L, 500_000L)

    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val isWithdraw = !showMethodSelector
    val isMinAmountValid = enteredAmount >= 10_000.0
    val isBalanceSufficient = !isWithdraw || (currentBalance == null || enteredAmount <= currentBalance)
    val isActionValid = isMinAmountValid && isBalanceSufficient

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXXL, topEnd = AppShape.ShapeXXL),
        containerColor = SurfaceLight,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeXLPlus)
                            .clip(CircleShape)
                            .background(if (showMethodSelector) PrimaryContainer else SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (showMethodSelector) Icons.Default.AccountBalanceWallet else Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (showMethodSelector) PrimaryColor else SecondaryColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.M))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.s18.bold(),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = if (showMethodSelector) {
                                stringResource(id = R.string.wallet_deposit_dialog_subtitle)
                            } else {
                                stringResource(id = R.string.wallet_withdraw_dialog_subtitle)
                            },
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    enabled = !isSubmitting
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = OutlineVariantLight)
            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Amount Input Card + Quick Presets
            WalletAmountInputSection(
                amountText = amountText,
                onAmountChange = { amountText = it },
                isMinAmountValid = isMinAmountValid,
                isWithdraw = isWithdraw,
                isBalanceSufficient = isBalanceSufficient,
                currentBalance = currentBalance,
                presets = presets,
                enteredAmount = enteredAmount
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Payment / Payout Method (VNPay only for deposit, Bank account for withdraw)
            WalletMethodCardSection(isDeposit = showMethodSelector)

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimen.HeightDefault),
                    shape = RoundedCornerShape(AppShape.PillShape),
                    enabled = !isSubmitting
                ) {
                    Text(
                        text = stringResource(id = R.string.wallet_btn_cancel),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = {
                        val method = if (showMethodSelector) "VNPAY" else "BANK_TRANSFER"
                        onConfirm(enteredAmount, method)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(Dimen.HeightDefault),
                    shape = RoundedCornerShape(AppShape.PillShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showMethodSelector) PrimaryColor else SecondaryColor
                    ),
                    enabled = isActionValid && !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = TextWhite,
                            modifier = Modifier.size(Dimen.SizeM),
                            strokeWidth = AppSpacing.XXS
                        )
                    } else {
                        Text(
                            text = if (showMethodSelector) {
                                stringResource(id = R.string.wallet_deposit_btn_confirm)
                            } else {
                                stringResource(id = R.string.wallet_withdraw_btn_confirm)
                            },
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
        }
    }
}
