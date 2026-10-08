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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16

@Composable
fun OrderDetailPaymentAndPricingSection(
    dropOffAddress: String,
    note: String,
    itemsPrice: Double,
    deliveryFee: Double?,
    totalPrice: Double?,
    paymentMethod: PaymentMethod?,
    isPaid: Boolean?,
    modifier: Modifier = Modifier
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
            // Delivery address
            Text(
                text = stringResource(id = R.string.checkout_delivery_address_title),
                style = MaterialTheme.typography.s15.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )

                Spacer(modifier = Modifier.width(AppSpacing.S))

                Text(
                    text = dropOffAddress,
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextPrimary
                )
            }

            if (note.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.S))
                Text(
                    text = "${stringResource(id = R.string.order_detail_note_title)}: $note",
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Pricing details
            Text(
                text = stringResource(id = R.string.order_detail_payment_title),
                style = MaterialTheme.typography.s15.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Items subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.checkout_price_subtotal),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Text(
                    text = formatFoodOrderPrice(itemsPrice),
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            // Delivery fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.checkout_price_delivery_fee),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Text(
                    text = formatFoodOrderPrice(deliveryFee),
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Total price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.checkout_price_total),
                    style = MaterialTheme.typography.s15.bold(),
                    color = TextPrimary
                )
                Text(
                    text = formatFoodOrderPrice(totalPrice),
                    style = MaterialTheme.typography.s16.bold(),
                    color = PrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Payment method & status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = when (paymentMethod) {
                            PaymentMethod.CASH -> stringResource(id = R.string.checkout_payment_cash)
                            PaymentMethod.WALLET -> stringResource(id = R.string.checkout_payment_wallet)
                            PaymentMethod.MOMO -> stringResource(id = R.string.checkout_payment_momo)
                            PaymentMethod.VNPAY -> stringResource(id = R.string.checkout_payment_vnpay)
                            null -> stringResource(id = R.string.order_detail_payment_method_unknown)
                        },
                        style = MaterialTheme.typography.s13.medium(),
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = if (isPaid == true) SuccessColor.copy(alpha = 0.12f) else SecondaryColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(AppShape.ShapeXS)
                        )
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPaid == true) Icons.Default.Check else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isPaid == true) SuccessColor else SecondaryColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Text(
                            text = when (isPaid) {
                                true -> stringResource(id = R.string.order_detail_payment_paid)
                                false -> stringResource(id = R.string.order_detail_payment_unpaid)
                                null -> stringResource(id = R.string.order_detail_payment_status_unknown)
                            },
                            style = MaterialTheme.typography.s12.medium(),
                            color = if (isPaid == true) SuccessColor else SecondaryColor
                        )
                    }
                }

            }
        }
    }
}
