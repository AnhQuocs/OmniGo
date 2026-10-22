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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import java.util.Locale

private data class TimelineStepDef(
    val titleRes: Int,
    val icon: ImageVector
)

@Composable
fun FoodOrderStatusTimeline(
    status: String,
    createdAt: String?,
    modifier: Modifier = Modifier
) {
    val currentStep = status.toFoodOrderTimelineStep() ?: 1
    val orderTime = formatFoodOrderTimeOnly(createdAt)

    val steps = listOf(
        TimelineStepDef(R.string.order_detail_step_placed, Icons.Default.Storefront),
        TimelineStepDef(R.string.order_detail_step_preparing, Icons.Default.OutdoorGrill),
        TimelineStepDef(R.string.order_detail_step_delivering, Icons.AutoMirrored.Filled.DirectionsBike),
        TimelineStepDef(R.string.order_detail_step_delivered, Icons.Default.Check)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingML, vertical = Dimen.PaddingM)
        ) {
            // Horizontal icons & connecting lines
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    val stepNumber = index + 1
                    val isCompleted = stepNumber <= currentStep

                    // Step Circle Badge
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeL)
                            .background(
                                color = if (isCompleted) SuccessColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = if (isCompleted) TextWhite else TextSecondary,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                    }

                    // Connector line
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimen.PaddingXXS)
                                .background(
                                    if (stepNumber < currentStep) SuccessColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Text labels under steps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, step ->
                    val stepNumber = index + 1
                    val isCompleted = stepNumber <= currentStep

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = step.titleRes),
                            style = if (isCompleted) MaterialTheme.typography.s12.bold() else MaterialTheme.typography.s12.normal(),
                            color = if (isCompleted) TextPrimary else TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.XXS))

                        Text(
                            text = if (stepNumber == 1 && orderTime.isNotBlank()) orderTime else "",
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
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
