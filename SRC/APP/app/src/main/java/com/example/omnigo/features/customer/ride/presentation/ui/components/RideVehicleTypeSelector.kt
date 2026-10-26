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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold
import java.text.DecimalFormat

data class VehicleOption(
    val type: VehicleType,
    val titleResId: Int,
    val descResId: Int,
    val icon: ImageVector
)

private val vehicleOptions = listOf(
    VehicleOption(
        type = VehicleType.BIKE,
        titleResId = R.string.ride_vehicle_bike_title,
        descResId = R.string.ride_vehicle_bike_desc,
        icon = Icons.AutoMirrored.Filled.DirectionsBike
    ),
    VehicleOption(
        type = VehicleType.CAR_4_SEAT,
        titleResId = R.string.ride_vehicle_car4_title,
        descResId = R.string.ride_vehicle_car4_desc,
        icon = Icons.Default.DirectionsCar
    ),
    VehicleOption(
        type = VehicleType.CAR_7_SEAT,
        titleResId = R.string.ride_vehicle_car7_title,
        descResId = R.string.ride_vehicle_car7_desc,
        icon = Icons.Default.ElectricCar
    )
)

@Composable
fun RideVehicleTypeSelector(
    selectedType: VehicleType,
    vehicleEstimates: Map<VehicleType, PricingEstimate>,
    isCalculating: Boolean,
    onSelectType: (VehicleType) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DecimalFormat("#,###")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
    ) {
        vehicleOptions.forEach { option ->
            val isSelected = option.type == selectedType
            val estimate = vehicleEstimates[option.type]

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectType(option.type) },
                shape = RectangleShape,
                color = if (isSelected) PrimaryContainer.copy(alpha = 0.35f) else Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = if (isSelected) PrimaryColor else TextSecondary,
                            modifier = Modifier.size(Dimen.SizeL)
                        )

                        Spacer(modifier = Modifier.width(AppSpacing.M))

                        Column {
                            Text(
                                text = stringResource(id = option.titleResId),
                                style = MaterialTheme.typography.s14.bold(),
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(id = option.descResId),
                                style = MaterialTheme.typography.s10.medium(),
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (isCalculating && estimate == null) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimen.SizeM),
                                strokeWidth = Dimen.PaddingXXS,
                                color = PrimaryColor
                            )
                        } else if (estimate != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${formatter.format(estimate.totalPrice)} đ",
                                    style = MaterialTheme.typography.s16.bold(),
                                    color = if (isSelected) PrimaryColor else TextPrimary
                                )
                                Text(
                                    text = "${estimate.distanceInKm} km • ~${estimate.estimatedDurationMinutes}p",
                                    style = MaterialTheme.typography.s10.medium(),
                                    color = TextSecondary
                                )
                            }
                        } else {
                            Text(
                                text = "-- đ",
                                style = MaterialTheme.typography.s14.semiBold(),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
