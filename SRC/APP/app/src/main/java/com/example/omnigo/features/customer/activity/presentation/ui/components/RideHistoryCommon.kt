package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.semiBold

private val BIKE_KEYS = setOf("BIKE", "OMNIBIKE")
private val CAR4_KEYS = setOf("CAR_4_SEAT", "CAR4", "OMNICAR4")
private val CAR7_KEYS = setOf("CAR_7_SEAT", "CAR7", "OMNICAR7")

private fun normalizeVehicle(raw: String?): String = raw?.uppercase()?.trim().orEmpty()

@Composable
internal fun rideVehicleLabel(vehicleType: String?): String = when (normalizeVehicle(vehicleType)) {
    in BIKE_KEYS -> stringResource(R.string.ride_vehicle_bike_title)
    in CAR4_KEYS -> stringResource(R.string.ride_vehicle_car4_title)
    in CAR7_KEYS -> stringResource(R.string.ride_vehicle_car7_title)
    else -> stringResource(R.string.home_service_ride)
}

internal fun rideVehicleIcon(vehicleType: String?): ImageVector =
    if (normalizeVehicle(vehicleType) in BIKE_KEYS) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar

@Composable
internal fun ridePaymentLabel(paymentMethod: String?): String =
    if (paymentMethod.equals("WALLET", ignoreCase = true)) {
        stringResource(R.string.activity_history_payment_wallet)
    } else {
        stringResource(R.string.activity_history_payment_cash)
    }

@Composable
internal fun RideVehicleBadge(
    vehicleType: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(Dimen.SizeXLPlus)
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .background(PrimaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = rideVehicleIcon(vehicleType),
            contentDescription = null,
            tint = PrimaryColor,
            modifier = Modifier.size(Dimen.SizeM)
        )
    }
}

@Composable
internal fun RideStatusChip(
    status: BookingStatus,
    modifier: Modifier = Modifier
) {
    val (label, icon, contentColor, bgColor) = when (status) {
        BookingStatus.COMPLETED -> StatusStyle(
            stringResource(R.string.activity_item_completed_badge),
            Icons.Default.CheckCircle,
            SuccessColor,
            SuccessColor.copy(alpha = 0.14f)
        )
        BookingStatus.CANCELLED -> StatusStyle(
            stringResource(R.string.activity_item_cancelled_badge),
            Icons.Default.Cancel,
            ErrorColor,
            ErrorContainer
        )
        else -> StatusStyle(
            stringResource(R.string.activity_item_ongoing_badge),
            Icons.Default.Schedule,
            PrimaryColor,
            PrimaryContainer
        )
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppShape.PillShape))
            .background(bgColor)
            .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(Dimen.SizeIconXS)
        )
        Spacer(modifier = Modifier.width(AppSpacing.XS))
        Text(
            text = label,
            style = MaterialTheme.typography.s10.semiBold(),
            color = contentColor,
            maxLines = 1
        )
    }
}

private data class StatusStyle(
    val label: String,
    val icon: ImageVector,
    val contentColor: androidx.compose.ui.graphics.Color,
    val bgColor: androidx.compose.ui.graphics.Color
)

/**
 * Hiển thị lộ trình dạng timeline: chấm xanh (điểm đón) nối chấm cam (điểm đến).
 * Khi truyền label, mỗi địa chỉ sẽ có thêm dòng nhãn nhỏ phía trên.
 */
@Composable
internal fun RideRouteTimeline(
    pickup: String,
    dropoff: String,
    modifier: Modifier = Modifier,
    pickupLabel: String? = null,
    dropoffLabel: String? = null,
    maxLines: Int = 1
) {
    val fallback = stringResource(R.string.ride_history_address_unknown)
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = AppSpacing.XS),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RouteDot(color = SuccessColor)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(Dimen.BorderThin)
                    .background(OutlineLight)
            )
            RouteDot(color = SecondaryColor)
        }
        Spacer(modifier = Modifier.width(AppSpacing.M))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
        ) {
            RouteStop(label = pickupLabel, address = pickup.ifBlank { fallback }, maxLines = maxLines)
            RouteStop(label = dropoffLabel, address = dropoff.ifBlank { fallback }, maxLines = maxLines)
        }
    }
}

@Composable
private fun RouteDot(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(AppSpacing.S)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun RouteStop(
    label: String?,
    address: String,
    maxLines: Int
) {
    Column {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.s12.normal(),
                color = TextTertiary
            )
        }
        Text(
            text = address,
            style = MaterialTheme.typography.s13.medium(),
            color = TextPrimary,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun RideInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.s13.normal(),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.width(AppSpacing.M))
        Text(
            text = value,
            style = MaterialTheme.typography.s13.semiBold(),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}
