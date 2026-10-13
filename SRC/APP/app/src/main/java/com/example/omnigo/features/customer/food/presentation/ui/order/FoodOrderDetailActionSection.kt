package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15

@Composable
fun FoodOrderDetailActionSection(
    order: FoodOrder,
    retryDriverCount: Int,
    isActionLoading: Boolean,
    onCancelClick: () -> Unit,
    onSwitchToCashClick: () -> Unit,
    onRetryDriverClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val upperStatus = order.status.uppercase()
    val isCancellable = when (upperStatus) {
        "PENDING",
        "AWAITING_PAYMENT",
        "NO_DRIVER_FOUND",
        "REJECTED",
        "ACCEPTED" -> true
        else -> false
    }

    val canSwitchToCash = order.paymentMethod != PaymentMethod.CASH &&
            order.isPaid != true &&
            (upperStatus == "PENDING" || upperStatus == "AWAITING_PAYMENT")

    val isNoDriverFound = upperStatus == "NO_DRIVER_FOUND"

    if (!isCancellable && !canSwitchToCash && !isNoDriverFound) {
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
        ) {
            Text(
                text = stringResource(id = R.string.order_detail_status_title),
                style = MaterialTheme.typography.s15.bold(),
                color = TextPrimary
            )

            // 1. Retry Driver option when NO_DRIVER_FOUND
            if (isNoDriverFound) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.XS)
                ) {
                    val remainingAttempts = (3 - retryDriverCount).coerceAtLeast(0)
                    Text(
                        text = stringResource(
                            id = R.string.order_detail_retry_driver_count,
                            remainingAttempts
                        ),
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextSecondary
                    )

                    Button(
                        onClick = onRetryDriverClick,
                        enabled = retryDriverCount < 3 && !isActionLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimen.SizeS),
                                color = TextWhite,
                                strokeWidth = Dimen.PaddingXXS
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimen.SizeS)
                                )
                                Text(
                                    text = stringResource(id = R.string.order_detail_btn_retry_driver),
                                    style = MaterialTheme.typography.s14.bold(),
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
            }

            // 2. Switch to Cash option
            if (canSwitchToCash) {
                OutlinedButton(
                    onClick = onSwitchToCashClick,
                    enabled = !isActionLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Text(
                            text = stringResource(id = R.string.order_detail_btn_switch_cash),
                            style = MaterialTheme.typography.s14.bold(),
                            color = PrimaryColor
                        )
                    }
                }
            }

            // 3. Cancel Order option
            if (isCancellable) {
                OutlinedButton(
                    onClick = onCancelClick,
                    enabled = !isActionLoading,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = ErrorColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Text(
                            text = stringResource(id = R.string.order_detail_btn_cancel),
                            style = MaterialTheme.typography.s14.bold(),
                            color = ErrorColor
                        )
                    }
                }
            }
        }
    }
}

