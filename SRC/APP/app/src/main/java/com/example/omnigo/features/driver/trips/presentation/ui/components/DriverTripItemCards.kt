package com.example.omnigo.features.driver.trips.presentation.ui.components

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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.OnSurfaceVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DriverActiveBookingCard(
    booking: Booking,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = PrimaryColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeL)
                        .clip(CircleShape)
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.S))

                Text(
                    text = stringResource(id = R.string.driver_trips_active_trip_title),
                    style = MaterialTheme.typography.s14.bold(),
                    color = PrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            Text(
                text = "${stringResource(id = R.string.driver_trip_dropoff_title)} #${booking.bookingId}",
                style = MaterialTheme.typography.s16.semiBold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            Text(
                text = String.format(Locale.getDefault(), "%,.0f ₫", booking.price),
                style = MaterialTheme.typography.s14.bold(),
                color = PrimaryColor
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Button(
                onClick = onResumeClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(AppShape.ShapeM),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.driver_trips_btn_resume_trip),
                    style = MaterialTheme.typography.s14.semiBold()
                )
            }
        }
    }
}

@Composable
fun DriverFoodOrderCard(
    order: DriverDeliveryOrder,
    isActive: Boolean,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCancelled = order.status == DriverDeliveryStatus.CANCELLED
    val badgeBg = when {
        isActive -> SecondaryContainer
        isCancelled -> ErrorContainer
        else -> SuccessColor.copy(alpha = 0.14f)
    }
    val badgeColor = when {
        isActive -> SecondaryColor
        isCancelled -> ErrorColor
        else -> SuccessColor
    }
    val badgeTextRes = when {
        isActive -> R.string.driver_delivery_badge_delivering
        isCancelled -> R.string.driver_trips_history_food_cancelled
        else -> R.string.driver_trips_history_food_completed
    }

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = if (isActive) SecondaryColor else CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeL)
                            .clip(CircleShape)
                            .background(SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            tint = SecondaryColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = "${stringResource(id = R.string.driver_trips_active_food_title)} #${order.orderId}",
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeS))
                        .background(badgeBg)
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                ) {
                    Text(
                        text = stringResource(id = badgeTextRes),
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            Text(
                text = order.restaurantName.ifBlank { stringResource(id = R.string.driver_delivery_default_name) },
                style = MaterialTheme.typography.s15.semiBold(),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (order.dropOffAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = order.dropOffAddress,
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            Text(
                text = String.format(
                    Locale.getDefault(),
                    "%,.0f ₫",
                    if (order.deliveryFee > 0) order.deliveryFee else order.totalPrice
                ),
                style = MaterialTheme.typography.s14.bold(),
                color = PrimaryColor
            )

            if (isActive) {
                Spacer(modifier = Modifier.height(AppSpacing.M))
                Button(
                    onClick = onResumeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor),
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_trips_btn_resume_food),
                        style = MaterialTheme.typography.s14.semiBold()
                    )
                }
            }
        }
    }
}

@Composable
fun DriverTripsEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingXL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_activity),
                contentDescription = null,
                tint = OnSurfaceVariantLight,
                modifier = Modifier.size(Dimen.SizeXXL)
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Text(
                text = message,
                style = MaterialTheme.typography.s15.normal(),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
