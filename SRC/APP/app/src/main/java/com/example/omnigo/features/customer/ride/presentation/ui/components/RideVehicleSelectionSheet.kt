package com.example.omnigo.features.customer.ride.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold
import java.text.DecimalFormat

private data class VehicleConfig(
    val type: VehicleType,
    val titleRes: Int,
    val descRes: Int,
    val seats: Int,
    val defaultEtaMins: Int,
    val imageRes: Int
)

private val vehicleConfigs = listOf(
    VehicleConfig(
        type = VehicleType.BIKE,
        titleRes = R.string.ride_vehicle_bike_title,
        descRes = R.string.ride_vehicle_bike_desc,
        seats = 1,
        defaultEtaMins = 3,
        imageRes = R.drawable.omnibike
    ),
    VehicleConfig(
        type = VehicleType.CAR_4_SEAT,
        titleRes = R.string.ride_vehicle_car4_title,
        descRes = R.string.ride_vehicle_car4_desc,
        seats = 4,
        defaultEtaMins = 5,
        imageRes = R.drawable.omnicar4
    ),
    VehicleConfig(
        type = VehicleType.CAR_7_SEAT,
        titleRes = R.string.ride_vehicle_car7_title,
        descRes = R.string.ride_vehicle_car7_desc,
        seats = 7,
        defaultEtaMins = 6,
        imageRes = R.drawable.omnicar7
    )
)

@Composable
fun RideVehicleSelectionSheet(
    selectedType: VehicleType,
    estimates: Map<VehicleType, PricingEstimate>,
    isCalculating: Boolean,
    onSelectVehicle: (VehicleType) -> Unit,
    onConfirmBooking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = AppShape.ShapeXXL, topEnd = AppShape.ShapeXXL),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Dimen.PaddingL
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle Bar
            Box(
                modifier = Modifier
                    .width(Dimen.SizeXL)
                    .height(Dimen.PaddingXS)
                    .clip(CircleShape)
                    .background(OutlineVariantLight)
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Vehicle Cards List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
            ) {
                vehicleConfigs.forEach { config ->
                    val isSelected = config.type == selectedType
                    val estimate = estimates[config.type]
                    VehicleItemCard(
                        config = config,
                        estimate = estimate,
                        isSelected = isSelected,
                        isCalculating = isCalculating,
                        onClick = { onSelectVehicle(config.type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            HorizontalDivider(color = OutlineVariantLight, thickness = Dimen.PaddingXXS)

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Payment and Voucher row (Xanh SM style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Payment Method
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeM))
                        .padding(horizontal = Dimen.PaddingXS, vertical = Dimen.PaddingXS)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = stringResource(id = R.string.ride_payment_cash_label),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }

                // Discount Voucher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeM))
                        .padding(horizontal = Dimen.PaddingXS, vertical = Dimen.PaddingXS)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = stringResource(id = R.string.ride_voucher_label),
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // CTA Confirm Booking Button
            val selectedEstimate = estimates[selectedType]
            val formattedPrice = selectedEstimate?.let {
                DecimalFormat("#,###").format(it.totalPrice) + " đ"
            } ?: ""

            Button(
                onClick = onConfirmBooking,
                enabled = !isCalculating && selectedEstimate != null,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                if (isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeSM),
                        color = OnPrimaryColor,
                        strokeWidth = Dimen.PaddingXXS
                    )
                } else {
                    val selectedTitle = stringResource(id = vehicleConfigs.first { it.type == selectedType }.titleRes)
                    Text(
                        text = if (formattedPrice.isNotBlank()) {
                            stringResource(id = R.string.ride_btn_book_vehicle_format, selectedTitle, formattedPrice)
                        } else {
                            stringResource(id = R.string.ride_confirm_booking)
                        },
                        style = MaterialTheme.typography.s16.bold(),
                        color = OnPrimaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleItemCard(
    config: VehicleConfig,
    estimate: PricingEstimate?,
    isSelected: Boolean,
    isCalculating: Boolean,
    onClick: () -> Unit
) {
    val containerBg = if (isSelected) PrimaryColor.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent

    Surface(
        shape = RoundedCornerShape(AppShape.ShapeM),
        color = containerBg,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle Image (without circle background)
            Image(
                painter = painterResource(id = config.imageRes),
                contentDescription = stringResource(id = config.titleRes),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(width = Dimen.SizeXXLPlus, height = Dimen.SizeXXL)
            )

            Spacer(modifier = Modifier.width(AppSpacing.M))

            // Vehicle Details (Title, Seats, ETA, Description)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = config.titleRes),
                        style = MaterialTheme.typography.s15.bold(),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    // Seats Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Text(
                            text = "${config.seats}",
                            style = MaterialTheme.typography.s12.medium(),
                            color = TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.XXS))

                // ETA Pickup
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XXS))
                    Text(
                        text = stringResource(id = R.string.ride_eta_pickup_format, config.defaultEtaMins),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextTertiary
                    )
                }

                Text(
                    text = stringResource(id = config.descRes),
                    style = MaterialTheme.typography.s10.normal(),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.S))

            // Fare Price Column
            Box(contentAlignment = Alignment.CenterEnd) {
                if (isCalculating && estimate == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeSM),
                        color = PrimaryColor,
                        strokeWidth = Dimen.PaddingXXS
                    )
                } else if (estimate != null) {
                    val formatted = DecimalFormat("#,###").format(estimate.totalPrice) + " đ"
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.s16.bold(),
                        color = if (isSelected) PrimaryColor else TextPrimary
                    )
                }
            }
        }
    }
}
