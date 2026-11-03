package com.example.omnigo.features.driver.trips.presentation.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrderItem
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.formatVnd
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.semiBold

@Composable
fun DriverOrderDetailCustomerCard(
    customerName: String,
    customerPhone: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = PrimaryContainer.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )
                Text(
                    text = customerPhone,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$customerPhone"))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(AppShape.ShapeM)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(Dimen.SizeS)
                )
                Spacer(modifier = Modifier.width(AppSpacing.XS))
                Text(
                    text = stringResource(id = R.string.driver_order_detail_call_customer),
                    style = MaterialTheme.typography.s12.bold()
                )
            }
        }
    }
}

@Composable
fun DriverOrderDetailRouteCard(
    foodOrder: DriverDeliveryOrder?,
    booking: Booking?,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = SurfaceLight,
        modifier = modifier
            .fillMaxWidth()
            .border(Dimen.PaddingXXS, CardBorderColor, RoundedCornerShape(AppShape.ShapeL))
    ) {
        Column(modifier = Modifier.padding(Dimen.PaddingM)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .clip(CircleShape)
                        .background(if (foodOrder != null) SecondaryContainer else PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (foodOrder != null) Icons.Default.Restaurant else Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = if (foodOrder != null) SecondaryColor else PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }
                Spacer(modifier = Modifier.width(AppSpacing.SPlus))
                Column {
                    Text(
                        text = if (foodOrder != null) {
                            foodOrder.restaurantName.ifBlank { stringResource(id = R.string.driver_order_detail_restaurant) }
                        } else {
                            stringResource(id = R.string.driver_trips_pickup_label)
                        },
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = TextSecondary
                    )
                    Text(
                        text = foodOrder?.restaurantAddress ?: booking?.pickupAddress ?: "—",
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = Dimen.PaddingS, top = Dimen.PaddingXXS, bottom = Dimen.PaddingXXS)
                    .width(Dimen.PaddingXXS)
                    .height(Dimen.SizeM)
                    .background(OutlineVariantLight)
            )

            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .clip(CircleShape)
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }
                Spacer(modifier = Modifier.width(AppSpacing.SPlus))
                Column {
                    Text(
                        text = stringResource(id = R.string.driver_order_detail_dropoff),
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = TextSecondary
                    )
                    Text(
                        text = foodOrder?.dropOffAddress ?: booking?.dropoffAddress ?: "—",
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun DriverOrderDetailItemsCard(
    items: List<DriverDeliveryOrderItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = SurfaceLight,
        modifier = modifier
            .fillMaxWidth()
            .border(Dimen.PaddingXXS, CardBorderColor, RoundedCornerShape(AppShape.ShapeL))
    ) {
        Column(modifier = Modifier.padding(Dimen.PaddingM)) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.quantity}x ${item.itemName}",
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.subtotal.formatVnd(),
                        style = MaterialTheme.typography.s13.semiBold(),
                        color = TextPrimary
                    )
                }
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Dimen.PaddingXS),
                        color = CardBorderColor.copy(alpha = 0.5f),
                        thickness = Dimen.PaddingXXS
                    )
                }
            }
        }
    }
}

@Composable
fun DriverOrderDetailFareCard(
    foodOrder: DriverDeliveryOrder?,
    booking: Booking?,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = SurfaceLight,
        modifier = modifier
            .fillMaxWidth()
            .border(Dimen.PaddingXXS, CardBorderColor, RoundedCornerShape(AppShape.ShapeL))
    ) {
        Column(modifier = Modifier.padding(Dimen.PaddingM), verticalArrangement = Arrangement.spacedBy(AppSpacing.S)) {
            Text(
                text = stringResource(id = R.string.driver_order_detail_fare_breakdown),
                style = MaterialTheme.typography.s14.bold(),
                color = TextPrimary
            )

            val fare = foodOrder?.deliveryFee ?: booking?.price ?: 0.0
            val netEarnings = fare * 0.80

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.driver_order_detail_delivery_fee),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Text(
                    text = fare.formatVnd(),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.driver_earnings_platform_fee_label),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Text(
                    text = "-${(fare * 0.20).formatVnd()}",
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
            }

            HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f), thickness = Dimen.PaddingXXS)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.driver_order_detail_net_earnings),
                    style = MaterialTheme.typography.s14.bold(),
                    color = SuccessColor
                )
                Text(
                    text = netEarnings.formatVnd(),
                    style = MaterialTheme.typography.s15.bold(),
                    color = SuccessColor
                )
            }

            HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f), thickness = Dimen.PaddingXXS)

            val paymentMethod = foodOrder?.paymentMethod ?: booking?.paymentMethod ?: "CASH"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.driver_order_detail_payment_method),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Text(
                    text = if (paymentMethod.equals("CASH", ignoreCase = true)) "Tiền mặt (COD)" else "Ví điện tử",
                    style = MaterialTheme.typography.s13.bold(),
                    color = PrimaryColor
                )
            }
        }
    }
}
