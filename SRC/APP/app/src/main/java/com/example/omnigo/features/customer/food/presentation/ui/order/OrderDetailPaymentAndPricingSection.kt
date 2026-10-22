package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
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

@Composable
fun OrderDetailPaymentAndPricingSection(
    paymentMethod: PaymentMethod?,
    isPaid: Boolean?,
    createdAt: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingML)
        ) {
            // Header: Suitcase/Wallet icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(Dimen.SizeM)
                )

                Spacer(modifier = Modifier.width(AppSpacing.S))

                Text(
                    text = stringResource(id = R.string.order_detail_payment_section_title),
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Row 1: Phương thức thanh toán
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
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = stringResource(id = R.string.order_detail_payment_method_label),
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextSecondary
                    )
                }

                // Payment method badge (e.g. Green pill Ví điện tử (Wallet) >)
                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(AppShape.ShapeS)
                        )
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = SuccessColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = when (paymentMethod) {
                            PaymentMethod.WALLET -> "Ví điện tử (Wallet)"
                            PaymentMethod.MOMO -> "Ví MoMo"
                            PaymentMethod.VNPAY -> "VNPay QR"
                            PaymentMethod.CASH -> stringResource(id = R.string.order_detail_payment_cash_desc)
                            null -> stringResource(id = R.string.order_detail_payment_method_unknown)
                        },
                        style = MaterialTheme.typography.s13.medium(),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XXS))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            // Row 2: Trạng thái thanh toán
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isPaid == true) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (isPaid == true) SuccessColor else SecondaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = stringResource(id = R.string.order_detail_payment_status_label),
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextSecondary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1.3f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPaid == true) {
                            Box(
                                modifier = Modifier
                                    .size(Dimen.SizeS)
                                    .background(SuccessColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SurfaceLight,
                                    modifier = Modifier.size(Dimen.SizeS2 / 2)
                                )
                            }
                            Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        }

                        Text(
                            text = if (isPaid == true) {
                                stringResource(id = R.string.order_detail_payment_paid_desc)
                            } else {
                                stringResource(id = R.string.order_detail_payment_unpaid_desc)
                            },
                            style = MaterialTheme.typography.s13.bold(),
                            color = if (isPaid == true) SuccessColor else SecondaryColor
                        )
                    }

                    if (isPaid == true && !createdAt.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        val formattedDateTime = formatFoodOrderDateTime(createdAt)
                        Text(
                            text = stringResource(id = R.string.order_detail_paid_at_format, formattedDateTime),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
