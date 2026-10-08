package com.example.omnigo.features.customer.food.presentation.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.presentation.ui.order.formatFoodOrderPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SecondaryVariant
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

@Composable
fun FoodOrderHistoryCard(
    order: FoodOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingML)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = order.restaurantName.ifBlank { stringResource(R.string.order_detail_restaurant_title) },
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.S))

                StatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            Text(
                text = stringResource(R.string.order_detail_order_id, order.id),
                style = MaterialTheme.typography.s12.normal(),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = OutlineVariantLight)
            Spacer(modifier = Modifier.height(AppSpacing.M))

            val itemsSummary = if (order.items.isNotEmpty()) {
                order.items.joinToString(", ") { "${it.quantity}x ${it.itemName}" }
            } else {
                stringResource(R.string.order_detail_value_unavailable)
            }

            Text(
                text = itemsSummary,
                style = MaterialTheme.typography.s14.normal(),
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val finalTotal = order.totalPrice ?: (order.itemsPrice + (order.deliveryFee ?: 0.0))
                    Text(
                        text = formatFoodOrderPrice(finalTotal),
                        style = MaterialTheme.typography.s16.bold(),
                        color = PrimaryColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.food_history_order_items_count, order.items.sumOf { it.quantity }),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.PaddingM)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (labelRes, bg, fg) = when (status.uppercase()) {
        "COMPLETED", "DELIVERED" -> Triple(R.string.order_detail_status_completed, SurfaceVariantLight, SuccessColor)
        "DELIVERING", "PICKED_UP", "PREPARING", "ACCEPTED", "PENDING" -> Triple(R.string.order_detail_status_delivering, SecondaryContainer, SecondaryVariant)
        "AWAITING_PAYMENT" -> Triple(R.string.order_detail_status_awaiting_payment, SecondaryContainer, PrimaryColor)
        "CANCELLED" -> Triple(R.string.order_detail_status_cancelled, ErrorContainer, ErrorColor)
        "REJECTED" -> Triple(R.string.order_detail_status_rejected, ErrorContainer, ErrorColor)
        "NO_DRIVER_FOUND" -> Triple(R.string.order_detail_status_no_driver, ErrorContainer, ErrorColor)
        else -> Triple(R.string.order_detail_status_unknown, SurfaceVariantLight, TextSecondary)
    }

    Box(
        modifier = Modifier
            .background(color = bg, shape = RoundedCornerShape(AppShape.ShapeXS))
            .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.s12.bold(),
            color = fg
        )
    }
}
