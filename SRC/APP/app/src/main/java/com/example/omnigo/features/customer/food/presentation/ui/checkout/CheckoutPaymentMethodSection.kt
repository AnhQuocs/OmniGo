package com.example.omnigo.features.customer.food.presentation.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add

import androidx.compose.material.icons.filled.Money

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.presentation.format.formatFoodPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.semiBold

@Composable
fun CheckoutPaymentMethodSection(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier,
    walletBalance: Double? = null,
    totalAmount: Double = 0.0,
    onTopUpClick: () -> Unit = {}
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppSpacing.XXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Text(
                text = stringResource(id = R.string.checkout_payment_method_title),
                style = MaterialTheme.typography.s15.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            val methods = listOf(
                Triple(PaymentMethod.WALLET, R.string.checkout_payment_wallet, Icons.Default.AccountBalanceWallet),
                Triple(PaymentMethod.CASH, R.string.checkout_payment_cash, Icons.Default.Money)
            )

            methods.forEachIndexed { index, (method, titleRes, icon) ->
                PaymentOptionItem(
                    method = method,
                    title = stringResource(id = titleRes),
                    icon = icon,
                    isSelected = selectedMethod == method,
                    walletBalance = walletBalance,
                    totalAmount = totalAmount,
                    onTopUpClick = onTopUpClick,
                    onClick = { onMethodSelected(method) }
                )
                if (index < methods.lastIndex) {
                    Spacer(modifier = Modifier.height(AppSpacing.S))
                }
            }
        }
    }
}

@Composable
private fun PaymentOptionItem(
    method: PaymentMethod,
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    walletBalance: Double?,
    totalAmount: Double,
    onTopUpClick: () -> Unit,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryColor else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) PrimaryColor.copy(alpha = 0.05f) else SurfaceLight

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeS),
        color = containerColor,
        border = BorderStroke(Dimen.BorderThin, borderColor)
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) PrimaryColor else TextSecondary,
                        modifier = Modifier.size(Dimen.SizeM)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.M))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = PrimaryColor,
                        unselectedColor = TextSecondary
                    )
                )
            }

            // Wallet details section
            if (method == PaymentMethod.WALLET) {
                val balance = walletBalance ?: 0.0
                val isInsufficient = isSelected && (balance < totalAmount)
                val deficit = (totalAmount - balance).coerceAtLeast(0.0)

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Dimen.SizeM + AppSpacing.M),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                id = R.string.checkout_wallet_balance_format,
                                formatFoodPrice(balance)
                            ),
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextSecondary
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.height(AppSpacing.XXS))
                            if (isInsufficient) {
                                Text(
                                    text = stringResource(
                                        id = R.string.checkout_wallet_insufficient,
                                        formatFoodPrice(deficit)
                                    ),
                                    style = MaterialTheme.typography.s12.medium(),
                                    color = ErrorColor
                                )
                            } else {
                                Text(
                                    text = stringResource(id = R.string.checkout_wallet_sufficient),
                                    style = MaterialTheme.typography.s12.medium(),
                                    color = SuccessColor
                                )
                            }
                        }
                    }

                    if (isInsufficient) {
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Surface(
                            onClick = onTopUpClick,
                            shape = RoundedCornerShape(AppShape.ShapeXS),
                            color = SecondaryColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = SurfaceLight,
                                    modifier = Modifier.size(Dimen.SizeS)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.XXS))
                                Text(
                                    text = stringResource(id = R.string.checkout_wallet_top_up_btn),
                                    style = MaterialTheme.typography.s12.bold(),
                                    color = SurfaceLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
