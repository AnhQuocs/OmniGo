package com.example.omnigo.features.customer.food.presentation.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.omnigo.features.customer.food.domain.util.FoodOrderHistoryHelper
import com.example.omnigo.features.customer.food.domain.util.OrderPaymentStatusType
import com.example.omnigo.features.customer.food.presentation.ui.order.formatFoodOrderPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14

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

    val thumbnailSummary = FoodOrderHistoryHelper.resolveThumbnails(order.items)
    val finalTotal = order.totalPrice ?: (order.itemsPrice + (order.deliveryFee ?: 0.0))
    val formattedTimeDate = FoodOrderHistoryHelper.formatTimeAndDate(order.createdAt)
    val etaRange = FoodOrderHistoryHelper.estimateOngoingEtaRange(order)
    val addressText = order.restaurantAddress?.takeIf { it.isNotBlank() } ?: order.dropOffAddress

    val paymentStatusText = when (FoodOrderHistoryHelper.resolvePaymentStatusType(order)) {
        OrderPaymentStatusType.REFUNDED -> stringResource(R.string.food_history_payment_status_refunded)
        OrderPaymentStatusType.PAID -> stringResource(R.string.food_history_payment_status_paid)
        OrderPaymentStatusType.CASH -> stringResource(R.string.food_history_payment_status_cash)
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeL)),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingSM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Large Square Restaurant / Food Image
            Box(
                modifier = Modifier
                    .size(Dimen.SizeHistoryCardImage)
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
                        modifier = Modifier.size(Dimen.SizeL)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.SPlus))

            // Right: Order Information Column
            Column(modifier = Modifier.weight(1f)) {
                // Row 1: Restaurant Name + Status Chip + Chevron
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.restaurantName.ifBlank {
                            stringResource(R.string.order_detail_restaurant_title)
                        },
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.XS))

                    FoodOrderHistoryStatusChip(status = order.status)

                    Spacer(modifier = Modifier.width(AppSpacing.XXS))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }

                // Row 2: Restaurant Address
                if (addressText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.XS))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(Dimen.ShimmerTextHeightM)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = addressText,
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Row 3: Order Time & Date + Ongoing ETA
                if (formattedTimeDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(Dimen.ShimmerTextHeightM)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Text(
                            text = formattedTimeDate,
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (etaRange != null) {
                            Spacer(modifier = Modifier.width(AppSpacing.XS))
                            Text(
                                text = "|",
                                style = MaterialTheme.typography.s12.normal(),
                                color = OutlineLight
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XS))
                            Text(
                                text = stringResource(
                                    R.string.food_history_eta_range,
                                    etaRange.first,
                                    etaRange.second
                                ),
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Row 4: Item Thumbnails (Left) + Price & Payment Info (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSPlus),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        thumbnailSummary.visibleItems.forEach { item ->
                            val thumbUrl = item.imageUrl?.takeIf { it.isNotBlank() } ?: displayImageUrl
                            OrderItemThumbnailBox(
                                imageUrl = thumbUrl,
                                contentDescription = item.itemName
                            )
                        }

                        if (thumbnailSummary.remainingQuantity > 0) {
                            Box(
                                modifier = Modifier
                                    .size(Dimen.SizeOrderItemThumbnail)
                                    .clip(RoundedCornerShape(AppShape.ShapeS))
                                    .background(SurfaceVariantLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.food_history_extra_items_badge,
                                        thumbnailSummary.remainingQuantity
                                    ),
                                    style = MaterialTheme.typography.s12.bold(),
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.XS))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatFoodOrderPrice(finalTotal),
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextPrimary,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.XXS))

                        Text(
                            text = stringResource(
                                R.string.food_history_items_and_payment_status,
                                thumbnailSummary.totalQuantity,
                                paymentStatusText
                            ),
                            style = MaterialTheme.typography.s10.normal(),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.XS))

                        FoodOrderPaymentMethodChip(paymentMethod = order.paymentMethod)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemThumbnailBox(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(Dimen.SizeOrderItemThumbnail)
            .clip(RoundedCornerShape(AppShape.ShapeS))
            .background(SurfaceVariantLight),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Fastfood,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(Dimen.SizeS)
            )
        }
    }
}
