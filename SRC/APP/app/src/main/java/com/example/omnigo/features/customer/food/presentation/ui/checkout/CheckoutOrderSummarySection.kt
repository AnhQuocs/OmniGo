package com.example.omnigo.features.customer.food.presentation.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.CartItem
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
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
import java.util.Locale

@Composable
fun CheckoutOrderSummarySection(
    restaurantName: String,
    items: List<CartItem>,
    itemsSubtotal: Double,
    deliveryFee: Double,
    totalAmount: Double,
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
            Text(
                text = stringResource(id = R.string.checkout_order_summary_title),
                style = MaterialTheme.typography.s15.bold(),
                color = TextPrimary
            )

            if (restaurantName.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = restaurantName,
                    style = MaterialTheme.typography.s13.medium(),
                    color = PrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            items.forEach { cartItem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.XXS),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${cartItem.quantity}x ${cartItem.menuItem.name}",
                            style = MaterialTheme.typography.s14.medium(),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (cartItem.note.isNotBlank()) {
                            Text(
                                text = "• ${cartItem.note}",
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextSecondary
                            )
                        }
                    }

                    Text(
                        text = String.format(Locale.US, "%,.0f ₫", cartItem.subtotalPrice),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Subtotal
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
                    text = String.format(Locale.US, "%,.0f ₫", itemsSubtotal),
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            // Delivery Fee
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
                    text = String.format(Locale.US, "%,.0f ₫", deliveryFee),
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Total
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
                    text = String.format(Locale.US, "%,.0f ₫", totalAmount),
                    style = MaterialTheme.typography.s16.bold(),
                    color = PrimaryColor
                )
            }
        }
    }
}
