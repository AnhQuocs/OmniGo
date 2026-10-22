package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import java.util.Locale

@Composable
fun FoodOrderDetailBottomBar(
    order: FoodOrder,
    retryDriverCount: Int,
    isActionLoading: Boolean,
    onCancelClick: () -> Unit,
    onSwitchToCashClick: () -> Unit,
    onRetryDriverClick: () -> Unit,
    onReorderClick: (FoodOrder) -> Unit,
    onReviewClick: (FoodOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val upperStatus = order.status.uppercase(Locale.ROOT)
    val isDelivered = upperStatus in setOf("DELIVERED", "COMPLETED")
    val isCancelled = upperStatus in setOf("CANCELLED", "REJECTED")

    val isCancellable = when (upperStatus) {
        "PENDING",
        "AWAITING_PAYMENT",
        "NO_DRIVER_FOUND",
        "ACCEPTED" -> true
        else -> false
    }

    val canSwitchToCash = order.paymentMethod != PaymentMethod.CASH &&
        order.isPaid != true &&
        (upperStatus == "PENDING" || upperStatus == "AWAITING_PAYMENT")

    val isNoDriverFound = upperStatus == "NO_DRIVER_FOUND"

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceLight,
        shadowElevation = Dimen.PaddingS
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingML, vertical = Dimen.PaddingM)
        ) {
            // Top Row: "Tổng cộng" + Price + Chevron Up
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.order_detail_total_summary_label),
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatFoodOrderPrice(order.totalPrice ?: (order.itemsPrice + (order.deliveryFee ?: 0.0))),
                        style = MaterialTheme.typography.s18.bold(),
                        color = PrimaryColor
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.XXS))

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Action Buttons
            when {
                // Delivered: [ Đánh giá ] (Outlined) + [ Đặt lại đơn này ] (Filled)
                isDelivered -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
                    ) {
                        OutlinedButton(
                            onClick = { onReviewClick(order) },
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimen.HeightDefault),
                            shape = RoundedCornerShape(AppShape.ShapeM),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeS2)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.S))
                            Text(
                                text = stringResource(id = R.string.food_history_btn_review),
                                style = MaterialTheme.typography.s14.bold(),
                                color = PrimaryColor
                            )
                        }

                        Button(
                            onClick = { onReorderClick(order) },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(Dimen.HeightDefault),
                            shape = RoundedCornerShape(AppShape.ShapeM),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(Dimen.SizeS2)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.S))
                            Text(
                                text = stringResource(id = R.string.order_detail_btn_reorder_this),
                                style = MaterialTheme.typography.s14.bold(),
                                color = TextWhite
                            )
                        }
                    }
                }

                // Cancelled / Rejected: [ Đặt lại đơn này ] (Filled)
                isCancelled -> {
                    Button(
                        onClick = { onReorderClick(order) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimen.HeightDefault),
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(Dimen.SizeS2)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = stringResource(id = R.string.order_detail_btn_reorder_this),
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextWhite
                        )
                    }
                }

                // Active orders: Retry driver / Switch to Cash / Cancel
                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
                    ) {
                        if (isNoDriverFound) {
                            Button(
                                onClick = onRetryDriverClick,
                                enabled = retryDriverCount < 3 && !isActionLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                shape = RoundedCornerShape(AppShape.ShapeM),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimen.HeightDefault)
                            ) {
                                if (isActionLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(Dimen.SizeS),
                                        color = TextWhite,
                                        strokeWidth = Dimen.PaddingXXS
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimen.SizeS)
                                    )
                                    Spacer(modifier = Modifier.width(AppSpacing.S))
                                    Text(
                                        text = stringResource(id = R.string.order_detail_btn_retry_driver),
                                        style = MaterialTheme.typography.s14.bold(),
                                        color = TextWhite
                                    )
                                }
                            }
                        }

                        if (canSwitchToCash) {
                            OutlinedButton(
                                onClick = onSwitchToCashClick,
                                enabled = !isActionLoading,
                                shape = RoundedCornerShape(AppShape.ShapeM),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimen.HeightDefault)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(Dimen.SizeS)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.S))
                                Text(
                                    text = stringResource(id = R.string.order_detail_btn_switch_cash),
                                    style = MaterialTheme.typography.s14.bold(),
                                    color = PrimaryColor
                                )
                            }
                        }

                        if (isCancellable) {
                            OutlinedButton(
                                onClick = onCancelClick,
                                enabled = !isActionLoading,
                                shape = RoundedCornerShape(AppShape.ShapeM),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimen.HeightDefault)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = ErrorColor,
                                    modifier = Modifier.size(Dimen.SizeS)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.S))
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
    }
}
