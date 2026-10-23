package com.example.omnigo.features.customer.ride.presentation.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun PricingEstimateSection(
    isCalculating: Boolean,
    pricingEstimate: PricingEstimate?,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimen.PaddingXXS
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Text(
                text = stringResource(id = R.string.ride_fare_summary),
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            when {
                isCalculating -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimen.PaddingS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimen.SizeM),
                            strokeWidth = Dimen.PaddingXXS,
                            color = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = stringResource(id = R.string.ride_estimating_price),
                            style = MaterialTheme.typography.s14.medium(),
                            color = TextSecondary
                        )
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.s12.medium(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = Dimen.PaddingXS)
                    )
                }
                pricingEstimate != null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(Dimen.SizeXL)
                                    .clip(RoundedCornerShape(AppShape.ShapeM))
                                    .background(PrimaryColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(Dimen.SizeM)
                                )
                            }
                            Spacer(modifier = Modifier.width(AppSpacing.S))
                            Column {
                                Text(
                                    text = "OmniRide Standard",
                                    style = MaterialTheme.typography.s16.bold(),
                                    color = TextPrimary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(Dimen.SizeS)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.ride_distance_format, pricingEstimate.distanceInKm),
                                        style = MaterialTheme.typography.s12.medium(),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.s12.medium(),
                                        color = TextSecondary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(Dimen.SizeS)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.ride_duration_format, pricingEstimate.estimatedDurationMinutes),
                                        style = MaterialTheme.typography.s12.medium(),
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Total Price from Backend
                        val formatter = java.text.DecimalFormat("#,###")
                        Text(
                            text = "${formatter.format(pricingEstimate.totalPrice)} đ",
                            style = MaterialTheme.typography.s18.bold(),
                            color = PrimaryColor
                        )
                    }
                }
                else -> {
                    Text(
                        text = stringResource(id = R.string.ride_dropoff_hint),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
