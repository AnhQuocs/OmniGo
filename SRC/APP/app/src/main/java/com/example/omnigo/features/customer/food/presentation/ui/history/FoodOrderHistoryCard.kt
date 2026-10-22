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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import com.example.omnigo.features.customer.food.presentation.ui.order.formatFoodOrderDateTime
import com.example.omnigo.features.customer.food.presentation.ui.order.formatFoodOrderPrice
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
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15

@Composable
fun FoodOrderHistoryCard(
    order: FoodOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayImageUrl = order.items.firstOrNull { !it.imageUrl.isNullOrBlank() }?.imageUrl
        ?: order.restaurantImageUrl.orEmpty()

    val totalItemCount = order.items.sumOf { it.quantity }.coerceAtLeast(1)
    val finalTotal = order.totalPrice ?: (order.itemsPrice + (order.deliveryFee ?: 0.0))
    val formattedDate = formatFoodOrderDateTime(order.createdAt)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Big Rounded Food / Restaurant Thumbnail on Left
            Box(
                modifier = Modifier
                    .size(Dimen.SizeMega)
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = PrimaryColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(Dimen.SizeXL)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            // Right Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Row 1: Restaurant Name + Status Pill Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.restaurantName.ifBlank { stringResource(R.string.order_detail_restaurant_title) },
                        style = MaterialTheme.typography.s15.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.S))

                    FoodOrderHistoryStatusChip(status = order.status)
                }

                Spacer(modifier = Modifier.height(AppSpacing.XXS))

                // Row 2: Formatted Date & Time (dd/MM/yyyy · HH:mm)
                if (formattedDate.isNotBlank()) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Row 3: Mini Dish Thumbnails Preview (up to 3 items + overflow badge)
                if (order.items.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.XSPlus)
                    ) {
                        val previewItems = order.items.take(3)
                        val extraCount = (order.items.size - 3).coerceAtLeast(0)

                        previewItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .size(Dimen.SizeXL)
                                    .clip(RoundedCornerShape(AppShape.ShapeS))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!item.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(item.imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = item.itemName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Fastfood,
                                        contentDescription = null,
                                        tint = PrimaryColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(Dimen.SizeS)
                                    )
                                }
                            }
                        }

                        if (extraCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(Dimen.SizeXL)
                                    .clip(RoundedCornerShape(AppShape.ShapeS))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$extraCount",
                                    style = MaterialTheme.typography.s12.medium(),
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.S))
                }

                // Row 4: Total items + Total Price + Chevron Arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(R.string.food_history_order_items_count, totalItemCount)} • ${formatFoodOrderPrice(finalTotal)}",
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }
            }
        }
    }
}
