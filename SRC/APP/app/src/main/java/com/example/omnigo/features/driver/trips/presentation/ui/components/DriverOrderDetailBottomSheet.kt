package com.example.omnigo.features.driver.trips.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverOrderDetailBottomSheet(
    foodOrder: DriverDeliveryOrder? = null,
    booking: Booking? = null,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM)
                .verticalScroll(scrollState)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.driver_order_detail_title),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                    val idText = when {
                        booking != null -> "Cuốc xe #${booking.bookingId}"
                        foodOrder != null -> "Đơn giao #${foodOrder.orderId}"
                        else -> ""
                    }
                    Text(
                        text = idText,
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.driver_order_detail_close),
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Customer Contact Section (if available)
            val customerPhone = foodOrder?.customerPhone?.ifBlank { null }
            val customerName = foodOrder?.customerName?.ifBlank { null } ?: "Khách hàng"
            if (customerPhone != null) {
                DriverOrderDetailCustomerCard(
                    customerName = customerName,
                    customerPhone = customerPhone
                )
                Spacer(modifier = Modifier.height(AppSpacing.M))
            }

            // Route Section
            DriverOrderDetailRouteCard(
                foodOrder = foodOrder,
                booking = booking
            )

            // Food Items Section (if Food Order)
            if (foodOrder != null && foodOrder.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppSpacing.M))
                Text(
                    text = stringResource(id = R.string.driver_order_detail_items, foodOrder.items.size),
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                DriverOrderDetailItemsCard(items = foodOrder.items)
            }

            // Customer Note (if any)
            val note = foodOrder?.note
            if (!note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.M))
                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    color = SecondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dimen.PaddingSM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stringResource(id = R.string.driver_order_detail_note)}: $note",
                            style = MaterialTheme.typography.s12.medium(),
                            color = SecondaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Fare & Earnings Section
            DriverOrderDetailFareCard(
                foodOrder = foodOrder,
                booking = booking
            )

            Spacer(modifier = Modifier.height(AppSpacing.L))

            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(AppShape.ShapeL),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                Text(
                    text = stringResource(id = R.string.driver_order_detail_close),
                    style = MaterialTheme.typography.s14.bold(),
                    color = OnPrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.XL))
        }
    }
}
