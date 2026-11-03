package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.activity.domain.util.ActivityHistoryHelper
import com.example.omnigo.features.customer.activity.presentation.viewmodel.HistoryServiceFilter
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.util.FoodOrderHistoryHelper
import com.example.omnigo.features.customer.food.domain.util.OrderHistoryStatusGroup
import com.example.omnigo.features.customer.food.presentation.ui.order.formatFoodOrderPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s13

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
            .border(Dimen.BorderHairline, SecondaryColor, shape)
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
