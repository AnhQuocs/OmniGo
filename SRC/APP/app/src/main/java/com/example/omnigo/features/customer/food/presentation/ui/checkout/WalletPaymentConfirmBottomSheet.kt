package com.example.omnigo.features.customer.food.presentation.ui.checkout

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.presentation.format.formatFoodPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s28
import com.example.omnigo.utils.semiBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletPaymentConfirmBottomSheet(
    amount: Double,
    currentBalance: Double,
    restaurantName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val remainingBalance = (currentBalance - amount).coerceAtLeast(0.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXXL, topEnd = AppShape.ShapeXXL),
        containerColor = SurfaceLight,
        dragHandle = null,
        modifier = modifier
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
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.M))

                    Column {
                        Text(
                            text = stringResource(id = R.string.checkout_wallet_confirm_title),
                            style = MaterialTheme.typography.s18.bold(),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = stringResource(id = R.string.checkout_payment_wallet),
                            style = MaterialTheme.typography.s12.medium(),
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    enabled = !isLoading
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

            // Amount Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.checkout_wallet_confirm_amount_label),
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                Text(
                    text = formatFoodPrice(amount),
                    style = MaterialTheme.typography.s28.bold(),
                    color = PrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // Details card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppShape.ShapeM),
                color = SurfaceVariantLight,
                border = BorderStroke(Dimen.BorderThin, OutlineVariantLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimen.PaddingM),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
                ) {
                    ConfirmDetailRow(
                        label = stringResource(id = R.string.checkout_wallet_confirm_restaurant),
                        value = restaurantName,
                        isEmphasized = true
                    )

                    ConfirmDetailRow(
                        label = stringResource(id = R.string.checkout_wallet_confirm_source),
                        value = stringResource(id = R.string.checkout_payment_wallet),
                        isEmphasized = false
                    )

                    ConfirmDetailRow(
                        label = stringResource(id = R.string.checkout_wallet_confirm_current_balance),
                        value = formatFoodPrice(currentBalance),
                        isEmphasized = false
                    )

                    HorizontalDivider(color = OutlineVariantLight)

                    ConfirmDetailRow(
                        label = stringResource(id = R.string.checkout_wallet_confirm_remaining_balance),
                        value = formatFoodPrice(remainingBalance),
                        isEmphasized = true,
                        valueColor = SuccessColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // Actions
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                shape = RoundedCornerShape(AppShape.ShapeXL),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = TextWhite,
                        modifier = Modifier.size(Dimen.SizeM),
                        strokeWidth = AppSpacing.XXS
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.checkout_wallet_confirm_btn_pay),
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(AppShape.ShapeXL),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                Text(
                    text = stringResource(id = R.string.checkout_wallet_confirm_btn_cancel),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ConfirmDetailRow(
    label: String,
    value: String,
    isEmphasized: Boolean,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.s13.normal(),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.width(AppSpacing.M))
        Text(
            text = value,
            style = if (isEmphasized) MaterialTheme.typography.s13.bold() else MaterialTheme.typography.s13.normal(),
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

