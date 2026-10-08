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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import java.util.Locale
import com.example.omnigo.R
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
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s18

@Composable
fun FoodOrderStatusTimeline(
    status: String,
    estimatedDeliveryMinutes: Int?,
    modifier: Modifier = Modifier
) {
    val statusInfo = getStatusDisplayInfo(status)
    val timelineStep = status.toFoodOrderTimelineStep()
    val isTerminal = status.isTerminalFoodOrderStatus()

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeXLPlus)
                            .background(statusInfo.accentColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusInfo.icon,
                            contentDescription = null,
                            tint = statusInfo.accentColor,
                            modifier = Modifier.size(Dimen.SizeM)
                        )
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.M))

                    Column {
                        Text(
                            text = stringResource(id = statusInfo.titleRes),
                            style = MaterialTheme.typography.s18.bold(),
                            color = statusInfo.accentColor
                        )
                        Text(
                            text = stringResource(id = statusInfo.descRes),
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextSecondary
                        )
                    }
                }
            }

            if (!isTerminal && estimatedDeliveryMinutes != null) {
                Spacer(modifier = Modifier.height(AppSpacing.M))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = PrimaryColor.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(AppShape.ShapeS)
                        )
                        .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.S))

                    Text(
                        text = stringResource(id = R.string.order_detail_eta_format, estimatedDeliveryMinutes),
                        style = MaterialTheme.typography.s13.medium(),
                        color = PrimaryColor
                    )
                }
            }

            if (timelineStep != null) {
                Spacer(modifier = Modifier.height(AppSpacing.M))
                OrderStatusStepBar(currentStep = timelineStep)
            }
        }
    }
}

@Composable
private fun OrderStatusStepBar(currentStep: Int) {
    val totalSteps = 4 // 1: Placed/Merchant, 2: Preparing, 3: Delivering, 4: Delivered

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            val isCompleted = i <= currentStep
            val isCurrent = i == currentStep

            Box(
                modifier = Modifier
                    .size(Dimen.SizeM)
                    .background(
                        color = if (isCompleted) PrimaryColor else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted && !isCurrent) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                } else {
                    Text(
                        text = i.toString(),
                        style = MaterialTheme.typography.s12.bold(),
                        color = if (isCompleted) TextWhite else TextSecondary
                    )
                }
            }

            if (i < totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimen.PaddingXXS)
                        .background(
                            if (i < currentStep) PrimaryColor else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

private data class StatusDisplayInfo(
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val accentColor: Color
)

private fun getStatusDisplayInfo(status: String): StatusDisplayInfo {
    return when (status.uppercase(Locale.ROOT)) {
        "AWAITING_PAYMENT", "PENDING_PAYMENT" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_awaiting_payment,
            descRes = R.string.order_detail_status_awaiting_payment_desc,
            icon = Icons.Default.Payment,
            accentColor = SecondaryColor
        )
        "PENDING", "PENDING_MERCHANT" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_pending,
            descRes = R.string.order_detail_status_pending_desc,
            icon = Icons.Default.Restaurant,
            accentColor = PrimaryColor
        )
        "ACCEPTED" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_accepted,
            descRes = R.string.order_detail_status_accepted_desc,
            icon = Icons.Default.Restaurant,
            accentColor = PrimaryColor
        )
        "PREPARING" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_preparing,
            descRes = R.string.order_detail_status_preparing_desc,
            icon = Icons.Default.Fastfood,
            accentColor = SecondaryColor
        )
        "DRIVER_SEARCHING", "READY_FOR_PICKUP" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_driver_searching,
            descRes = R.string.order_detail_status_driver_searching_desc,
            icon = Icons.Default.PersonSearch,
            accentColor = PrimaryColor
        )
        "DRIVER_ASSIGNED" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_driver_assigned,
            descRes = R.string.order_detail_status_driver_assigned_desc,
            icon = Icons.Default.DeliveryDining,
            accentColor = PrimaryColor
        )
        "PICKED_UP" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_picked_up,
            descRes = R.string.order_detail_status_picked_up_desc,
            icon = Icons.Default.DeliveryDining,
            accentColor = PrimaryColor
        )
        "DELIVERING" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_delivering,
            descRes = R.string.order_detail_status_delivering_desc,
            icon = Icons.Default.DeliveryDining,
            accentColor = PrimaryColor
        )
        "ARRIVED_CUSTOMER" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_arrived,
            descRes = R.string.order_detail_status_arrived_desc,
            icon = Icons.Default.Navigation,
            accentColor = SuccessColor
        )
        "COMPLETED", "DELIVERED" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_completed,
            descRes = R.string.order_detail_status_completed_desc,
            icon = Icons.Default.CheckCircle,
            accentColor = SuccessColor
        )
        "CANCELLED" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_cancelled,
            descRes = R.string.order_detail_status_cancelled_desc,
            icon = Icons.Default.Fastfood,
            accentColor = ErrorColor
        )
        "NO_DRIVER_FOUND" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_no_driver,
            descRes = R.string.order_detail_status_no_driver_desc,
            icon = Icons.Default.PersonSearch,
            accentColor = SecondaryColor
        )
        "REJECTED" -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_rejected,
            descRes = R.string.order_detail_status_rejected_desc,
            icon = Icons.Default.Fastfood,
            accentColor = ErrorColor
        )
        else -> StatusDisplayInfo(
            titleRes = R.string.order_detail_status_unknown,
            descRes = R.string.order_detail_status_unknown_desc,
            icon = Icons.Default.Fastfood,
            accentColor = PrimaryColor
        )
    }
}

internal fun String.toFoodOrderTimelineStep(): Int? =
    when (uppercase(Locale.ROOT)) {
        "AWAITING_PAYMENT",
        "PENDING_PAYMENT",
        "PENDING",
        "PENDING_MERCHANT",
        "ACCEPTED" -> 1
        "PREPARING",
        "DRIVER_SEARCHING",
        "READY_FOR_PICKUP",
        "NO_DRIVER_FOUND" -> 2
        "DRIVER_ASSIGNED",
        "PICKED_UP",
        "DELIVERING",
        "ARRIVED_CUSTOMER" -> 3
        "COMPLETED",
        "DELIVERED" -> 4
        else -> null
    }

internal fun String.isTerminalFoodOrderStatus(): Boolean =
    uppercase(Locale.ROOT) in setOf("COMPLETED", "DELIVERED", "CANCELLED", "REJECTED")
