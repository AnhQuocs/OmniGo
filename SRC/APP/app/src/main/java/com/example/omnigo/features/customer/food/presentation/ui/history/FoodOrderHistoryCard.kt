package com.example.omnigo.features.customer.food.presentation.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.presentation.ui.order.formatFoodOrderDateTime
import com.example.omnigo.features.customer.food.presentation.ui.order.formatFoodOrderPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
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
fun FoodOrderHistoryCard(
    order: FoodOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onReorderClick: ((FoodOrder) -> Unit)? = null,
    onReviewClick: ((FoodOrder) -> Unit)? = null,
    onTrackClick: ((FoodOrder) -> Unit)? = null
) {
    val displayImageUrl = order.restaurantImageUrl.orEmpty().ifBlank {
        order.items.firstOrNull { !it.imageUrl.isNullOrBlank() }?.imageUrl.orEmpty()
    }

    val totalItemCount = order.items.sumOf { it.quantity }.coerceAtLeast(1)
    val finalTotal = order.totalPrice ?: (order.itemsPrice + (order.deliveryFee ?: 0.0))
    val formattedDate = formatFoodOrderDateTime(order.createdAt)

    val upperStatus = order.status.uppercase(Locale.ROOT)
    val isDelivered = upperStatus in setOf("COMPLETED", "DELIVERED")
    val isCancelled = upperStatus in setOf("CANCELLED", "REJECTED")
    val isOngoing = !isDelivered && !isCancelled

    val dishesSummary = order.items.joinToString(", ") { item ->
        if (item.quantity > 1) "${item.itemName} × ${item.quantity}" else item.itemName
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            // Header: Restaurant Avatar + Info + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeXXL)
                        .clip(RoundedCornerShape(AppShape.ShapeM))
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (displayImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(displayImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = order.restaurantName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeM)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.M))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.restaurantName.ifBlank { stringResource(R.string.order_detail_restaurant_title) },
                        style = MaterialTheme.typography.s15.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (formattedDate.isNotBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.S))

                FoodOrderHistoryStatusChip(status = order.status)
            }

            // Dishes Summary
            if (dishesSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.SPlus))
                Text(
                    text = dishesSummary,
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Pricing Row: Total Items + Final Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.food_history_order_items_count, totalItemCount),
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextSecondary
                )

                Text(
                    text = formatFoodOrderPrice(finalTotal),
                    style = MaterialTheme.typography.s16.bold(),
                    color = PrimaryColor
                )
            }

            // Quick Actions Footer
            Spacer(modifier = Modifier.height(AppSpacing.M))

            when {
                isOngoing -> {
                    Button(
                        onClick = { onTrackClick?.invoke(order) ?: onClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimen.HeightDefault),
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = null,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = stringResource(id = R.string.food_history_btn_track_order),
                            style = MaterialTheme.typography.s13.bold(),
                            color = TextWhite
                        )
                    }
                }
                isDelivered -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                    ) {
                        OutlinedButton(
                            onClick = { onReviewClick?.invoke(order) },
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimen.HeightDefault),
                            shape = RoundedCornerShape(AppShape.ShapeM)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XS))
                            Text(
                                text = stringResource(id = R.string.food_history_btn_review),
                                style = MaterialTheme.typography.s13.bold(),
                                color = PrimaryColor
                            )
                        }

                        Button(
                            onClick = { onReorderClick?.invoke(order) },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(Dimen.HeightDefault),
                            shape = RoundedCornerShape(AppShape.ShapeM),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XS))
                            Text(
                                text = stringResource(id = R.string.food_history_btn_reorder),
                                style = MaterialTheme.typography.s13.bold(),
                                color = TextWhite
                            )
                        }
                    }
                }
                isCancelled -> {
                    OutlinedButton(
                        onClick = { onReorderClick?.invoke(order) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimen.HeightDefault),
                        shape = RoundedCornerShape(AppShape.ShapeM)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = stringResource(id = R.string.food_history_btn_reorder),
                            style = MaterialTheme.typography.s13.bold(),
                            color = PrimaryColor
                        )
                    }
                }
            }
        }
    }
}
