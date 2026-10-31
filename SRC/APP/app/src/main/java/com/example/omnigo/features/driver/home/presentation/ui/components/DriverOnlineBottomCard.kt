package com.example.omnigo.features.driver.home.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.common.ui.components.DriverSwipeButton
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OnSurfaceVariantLight
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s18

private val OnlineEmeraldGreen = Color(0xFF00A86B)

@Composable
fun DriverOnlineBottomCard(
    isOnline: Boolean,
    isUpdating: Boolean,
    activeRideBooking: Booking?,
    activeFoodOrder: DriverDeliveryOrder?,
    onToggleStatus: (Boolean) -> Unit,
    onResumeRide: (Booking) -> Unit,
    onResumeFoodOrder: (DriverDeliveryOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = if (isOnline) SuccessColor else OnSurfaceVariantLight,
        animationSpec = tween(durationMillis = 300),
        label = "cockpitStatusColor"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.SPlus)
    ) {
        // Active Ongoing Trip Banner (if any)
        activeRideBooking?.let { booking ->
            ActiveTaskBannerCard(
                label = stringResource(id = R.string.driver_cockpit_active_ride_banner, booking.bookingId),
                actionText = stringResource(id = R.string.driver_trips_btn_resume_trip),
                onClick = { onResumeRide(booking) }
            )
        }

        // Active Ongoing Food Order Banner (if any)
        activeFoodOrder?.let { order ->
            ActiveTaskBannerCard(
                label = stringResource(id = R.string.driver_cockpit_active_food_banner, order.orderId),
                actionText = stringResource(id = R.string.driver_trips_btn_resume_food),
                onClick = { onResumeFoodOrder(order) }
            )
        }

        // Main Bottom Online Box (NO Card/Surface tint, pure crisp SurfaceLight white)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = Dimen.PaddingS,
                    shape = RoundedCornerShape(AppShape.ShapeXL),
                    clip = false
                )
                .clip(RoundedCornerShape(AppShape.ShapeXL))
                .background(SurfaceLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingM),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
            ) {
                // Status Header Row (No Switch - Anti-accidental tap)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.SPlus)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.PaddingSM)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                id = if (isOnline) R.string.driver_status_online else R.string.driver_status_offline
                            ),
                            style = MaterialTheme.typography.s18.bold(),
                            color = if (isOnline) SuccessColor else TextPrimary
                        )
                        Text(
                            text = stringResource(
                                id = if (isOnline) R.string.driver_status_online_hint else R.string.driver_status_offline_hint
                            ),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                    }
                }

                // Swipe to Confirm Play Button
                DriverSwipeButton(
                    text = stringResource(
                        id = if (isOnline) R.string.driver_home_btn_offline else R.string.driver_home_btn_online
                    ),
                    onSwipeConfirm = { onToggleStatus(!isOnline) },
                    isLoading = isUpdating,
                    trackColor = if (isOnline) ErrorColor else OnlineEmeraldGreen,
                    thumbColor = SurfaceLight,
                    contentColor = OnPrimaryColor,
                    thumbIcon = if (isOnline) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    thumbIconTint = if (isOnline) ErrorColor else OnlineEmeraldGreen,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ActiveTaskBannerCard(label: String, actionText: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = Dimen.PaddingS,
                shape = RoundedCornerShape(AppShape.ShapeL),
                clip = false
            )
            .clip(RoundedCornerShape(AppShape.ShapeL))
            .background(SecondaryColor)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.s13.bold(),
                color = OnPrimaryColor,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(AppSpacing.S))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .background(SurfaceLight)
                .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.s12.bold(),
                    color = SecondaryColor
                )
            }
        }
    }
}
