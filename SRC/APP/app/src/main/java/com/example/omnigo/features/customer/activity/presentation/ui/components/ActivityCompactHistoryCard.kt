package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.activity.domain.util.ActivityHistoryHelper
import com.example.omnigo.features.customer.activity.presentation.viewmodel.HistoryServiceFilter
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.util.FoodOrderHistoryHelper
import com.example.omnigo.features.customer.food.domain.util.OrderHistoryStatusGroup
import com.example.omnigo.features.customer.food.presentation.ui.order.formatFoodOrderPrice
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun ActivityServiceFilterRow(
    selectedService: HistoryServiceFilter,
    onServiceSelected: (HistoryServiceFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingSM),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServiceFilterChipItem(
            title = stringResource(R.string.activity_service_omniride),
            icon = Icons.Default.DirectionsCar,
            isSelected = selectedService == HistoryServiceFilter.OMNI_RIDE,
            onClick = { onServiceSelected(HistoryServiceFilter.OMNI_RIDE) }
        )

        ServiceFilterChipItem(
            title = stringResource(R.string.activity_service_omnifood),
            icon = Icons.Default.Restaurant,
            isSelected = selectedService == HistoryServiceFilter.OMNI_FOOD,
            onClick = { onServiceSelected(HistoryServiceFilter.OMNI_FOOD) }
        )
    }
}

@Composable
private fun ServiceFilterChipItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) SecondaryContainer else SurfaceVariantLight
    val contentColor = if (isSelected) SecondaryColor else TextSecondary
    val shape = RoundedCornerShape(AppShape.ShapeXL)

    val chipModifier = if (isSelected) {
        modifier
            .clip(shape)
            .background(bgColor)
            .border(Dimen.PaddingXXS / 2, SecondaryColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
    } else {
        modifier
            .clip(shape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
    }

    Row(
        modifier = chipModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(Dimen.SizeS2)
        )
        Spacer(modifier = Modifier.width(AppSpacing.S))
        Text(
            text = title,
            style = if (isSelected) {
                MaterialTheme.typography.s13.bold()
            } else {
                MaterialTheme.typography.s13.medium()
            },
            color = contentColor
        )
    }
}

@Composable
fun ActivityCompactFoodHistoryCard(
    order: FoodOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayImageUrl = order.restaurantImageUrl.orEmpty().ifBlank {
        order.items.firstOrNull { !it.imageUrl.isNullOrBlank() }?.imageUrl.orEmpty()
    }
    val addressText = order.restaurantAddress?.takeIf { it.isNotBlank() } ?: order.dropOffAddress
    val dateAndTime = ActivityHistoryHelper.formatCompactDateAndTime(order.createdAt)
    val finalTotal = order.totalPrice ?: (order.itemsPrice + (order.deliveryFee ?: 0.0))
    val totalQty = ActivityHistoryHelper.totalItemQuantity(order.items)
    val statusGroup = FoodOrderHistoryHelper.resolveStatusGroup(order.status)
    val isCancelled = statusGroup == OrderHistoryStatusGroup.CANCELLED

    val paymentMethodLabel = when (order.paymentMethod) {
        PaymentMethod.WALLET, PaymentMethod.MOMO -> stringResource(R.string.activity_history_payment_wallet)
        PaymentMethod.VNPAY -> stringResource(R.string.activity_history_payment_card)
        else -> stringResource(R.string.activity_history_payment_cash)
    }

    val summarySubtext = stringResource(
        R.string.activity_history_food_summary,
        totalQty,
        paymentMethodLabel
    )

    CompactHistoryCardLayout(
        title = order.restaurantName.ifBlank { stringResource(R.string.order_detail_restaurant_title) },
        subtitle = addressText,
        dateTimeText = dateAndTime,
        priceText = formatFoodOrderPrice(finalTotal),
        bottomRightSummaryText = summarySubtext,
        statusLabel = if (isCancelled) {
            stringResource(R.string.food_history_status_cancelled)
        } else {
            stringResource(R.string.food_history_status_delivered)
        },
        isCancelled = isCancelled,
        imageUrl = displayImageUrl,
        fallbackIcon = Icons.Default.Storefront,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun ActivityCompactRideHistoryCard(
    booking: Booking,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCancelled = booking.status == BookingStatus.CANCELLED
    val title = booking.dropoffAddress.ifBlank {
        "${stringResource(R.string.home_service_ride)} #${booking.bookingId}"
    }
    val subtitle = booking.pickupAddress.ifBlank {
        stringResource(R.string.activity_mock_ride_address)
    }
    val priceText = String.format(Locale.getDefault(), "%,.0fđ", booking.price)
    val paymentLabel = if (booking.paymentMethod.equals("WALLET", ignoreCase = true)) {
        stringResource(R.string.activity_history_payment_wallet)
    } else {
        stringResource(R.string.activity_history_payment_cash)
    }
    val distKm = booking.distanceInKm.takeIf { it > 0.0 } ?: 2.5
    val summarySubtext = stringResource(
        R.string.activity_history_ride_summary,
        distKm,
        paymentLabel
    )

    CompactHistoryCardLayout(
        title = title,
        subtitle = subtitle,
        dateTimeText = "#${booking.bookingId}",
        priceText = priceText,
        bottomRightSummaryText = summarySubtext,
        statusLabel = if (isCancelled) {
            stringResource(R.string.activity_item_cancelled_badge)
        } else {
            stringResource(R.string.activity_item_completed_badge)
        },
        isCancelled = isCancelled,
        imageUrl = "",
        fallbackIcon = Icons.Default.DirectionsCar,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun CompactHistoryCardLayout(
    title: String,
    subtitle: String,
    dateTimeText: String,
    priceText: String,
    bottomRightSummaryText: String,
    statusLabel: String,
    isCancelled: Boolean,
    imageUrl: String,
    fallbackIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            Box(
                modifier = Modifier
                    .size(Dimen.SizeActivityCompactImage)
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeL)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.SPlus))

            Column(modifier = Modifier.weight(1f)) {
                // Row 1: Title + Status Badge + Chevron
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.XS))

                    CompactStatusBadge(
                        text = statusLabel,
                        isCancelled = isCancelled
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.XXS))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }

                // Row 2: Address / Subtitle
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                // Row 3: Date/Time (Left) + Price (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateTimeText,
                        style = MaterialTheme.typography.s10.normal(),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.XS))

                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.XXS))

                // Row 4: Right-aligned items & payment summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = bottomRightSummaryText,
                        style = MaterialTheme.typography.s10.normal(),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactStatusBadge(
    text: String,
    isCancelled: Boolean
) {
    val bgColor = if (isCancelled) ErrorContainer else SuccessColor.copy(alpha = 0.14f)
    val contentColor = if (isCancelled) ErrorColor else SuccessColor
    val icon = if (isCancelled) Icons.Default.Cancel else Icons.Default.CheckCircle

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AppShape.ShapeXL))
            .background(bgColor)
            .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(Dimen.SizeIconXS)
        )
        Spacer(modifier = Modifier.width(AppSpacing.XXS))
        Text(
            text = text,
            style = MaterialTheme.typography.s10.semiBold(),
            color = contentColor,
            maxLines = 1
        )
    }
}

