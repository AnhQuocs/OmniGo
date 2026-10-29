package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.activity.domain.util.ActivityHistoryHelper
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

private data class OngoingFoodStepDef(
    val titleRes: Int,
    val icon: ImageVector,
    val timeText: String
)

@Composable
fun OngoingFoodActivityCard(
    order: FoodOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStep = ActivityHistoryHelper.resolveFoodTimelineStep(order.status)
    val confirmedTime = ActivityHistoryHelper.formatConfirmedTime(order.createdAt)
    val preparingTime = ActivityHistoryHelper.formatPreparingTime(order.createdAt, currentStep)
    val deliveryWindow = ActivityHistoryHelper.calculateDeliveryWindow(order)
    val totalQty = ActivityHistoryHelper.totalItemQuantity(order.items)
    val itemsSummaryText = ActivityHistoryHelper.summarizeFoodItemsText(order.items)
    val addressText = order.restaurantAddress?.takeIf { it.isNotBlank() } ?: order.dropOffAddress
    val thumbUrl = order.items.firstOrNull { !it.imageUrl.isNullOrBlank() }?.imageUrl
        ?: order.restaurantImageUrl.orEmpty()

    val statusBadgeText = when (currentStep) {
        1 -> stringResource(R.string.activity_food_status_confirmed)
        2 -> stringResource(R.string.activity_food_status_preparing)
        3 -> stringResource(R.string.activity_food_status_delivering)
        else -> stringResource(R.string.activity_food_status_completed)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            // Header Row: OmniFood Service Chip & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeXL))
                        .background(SecondaryContainer)
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = null,
                        tint = SecondaryColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = stringResource(R.string.activity_service_omnifood),
                        style = MaterialTheme.typography.s12.bold(),
                        color = SecondaryColor
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeXL))
                        .background(SecondaryContainer)
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SecondaryColor,
                        modifier = Modifier.size(Dimen.SizeIconXS)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = statusBadgeText,
                        style = MaterialTheme.typography.s12.bold(),
                        color = SecondaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Restaurant Name & Address Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.restaurantName.ifBlank {
                            stringResource(R.string.order_detail_restaurant_title)
                        },
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (addressText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = addressText,
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(Dimen.SizeSM)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            // 4-Step Progress Timeline
            OngoingFoodTimelineRow(
                currentStep = currentStep,
                confirmedTime = confirmedTime,
                preparingTime = preparingTime
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = OutlineVariantLight, thickness = Dimen.PaddingXXS / 2)
            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Order Items Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeActivityFoodSummaryThumb)
                        .clip(RoundedCornerShape(AppShape.ShapeS))
                        .background(SurfaceVariantLight),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(thumbUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = order.restaurantName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = null,
                            tint = SecondaryColor,
                            modifier = Modifier.size(Dimen.SizeM)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.M))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.activity_food_items_count_format, totalQty),
                        style = MaterialTheme.typography.s13.bold(),
                        color = TextPrimary
                    )
                    if (itemsSummaryText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = itemsSummaryText,
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(Dimen.SizeSM)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = OutlineVariantLight, thickness = Dimen.PaddingXXS / 2)
            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Footer: Estimated Delivery Time + "Xem chi tiết" OutlinedButton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Column {
                        Text(
                            text = stringResource(R.string.activity_food_est_delivery_label),
                            style = MaterialTheme.typography.s10.normal(),
                            color = TextSecondary
                        )
                        Text(
                            text = "${deliveryWindow.startTimeText} – ${deliveryWindow.endTimeText} (${deliveryWindow.maxMinutes} ${stringResource(R.string.ride_live_tracking_eta_min_format, deliveryWindow.maxMinutes).substringAfter(" ")})",
                            style = MaterialTheme.typography.s13.bold(),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.S))

                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(AppShape.ShapeXL),
                    border = BorderStroke(Dimen.PaddingXXS / 2, SecondaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryColor),
                    contentPadding = PaddingValues(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
                ) {
                    Text(
                        text = stringResource(R.string.activity_food_btn_view_details),
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = SecondaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun OngoingFoodTimelineRow(
    currentStep: Int,
    confirmedTime: String,
    preparingTime: String
) {
    val steps = listOf(
        OngoingFoodStepDef(R.string.activity_food_status_confirmed, Icons.Default.Check, confirmedTime),
        OngoingFoodStepDef(R.string.activity_food_status_preparing, Icons.Default.OutdoorGrill, preparingTime),
        OngoingFoodStepDef(R.string.activity_food_status_delivering, Icons.Default.Restaurant, ""),
        OngoingFoodStepDef(R.string.activity_food_status_completed, Icons.AutoMirrored.Filled.FactCheck, "")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                val stepNum = index + 1
                val isCompletedOrCurrent = stepNum <= currentStep
                val isCurrent = stepNum == currentStep

                Box(
                    modifier = Modifier
                        .size(if (isCurrent) Dimen.SizeL else Dimen.SizeML)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> SecondaryContainer
                                isCompletedOrCurrent -> SecondaryColor
                                else -> OutlineVariantLight
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = when {
                            isCurrent -> SecondaryColor
                            isCompletedOrCurrent -> TextWhite
                            else -> TextSecondary
                        },
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }

                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimen.PaddingXXS)
                            .background(
                                if (stepNum < currentStep) SecondaryColor else OutlineVariantLight
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.XS))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, step ->
                val stepNum = index + 1
                val isCompletedOrCurrent = stepNum <= currentStep
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(step.titleRes),
                        style = if (isCompletedOrCurrent) {
                            MaterialTheme.typography.s10.bold()
                        } else {
                            MaterialTheme.typography.s10.normal()
                        },
                        color = if (isCompletedOrCurrent) TextPrimary else TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (step.timeText.isNotBlank()) {
                        Text(
                            text = step.timeText,
                            style = MaterialTheme.typography.s10.normal(),
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

