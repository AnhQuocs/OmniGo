package com.example.omnigo.features.customer.ride.presentation.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun ActiveTripRouteCard(
    booking: Booking,
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
        Column(modifier = Modifier.padding(Dimen.PaddingL)) {
            // Pickup
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SuccessColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.M))
                Column {
                    Text(
                        text = stringResource(id = R.string.ride_pickup_label),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Text(
                        text = stringResource(
                            id = R.string.driver_ride_offer_coordinates_format,
                            booking.startLatitude,
                            booking.startLongitude
                        ),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }
            }

            // Connector
            Box(
                modifier = Modifier
                    .padding(start = Dimen.PaddingS)
                    .width(Dimen.PaddingXXS)
                    .height(Dimen.PaddingL)
                    .background(OutlineVariantLight)
            )

            // Dropoff
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = ErrorColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.M))
                Column {
                    Text(
                        text = stringResource(id = R.string.ride_dropoff_label),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Text(
                        text = stringResource(
                            id = R.string.driver_ride_offer_coordinates_format,
                            booking.endLatitude,
                            booking.endLongitude
                        ),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

            // Fare & Payment Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedPrice = String.format(Locale.getDefault(), "%,.0f đ", booking.price)
                Text(
                    text = "${stringResource(id = R.string.ride_fare_summary)}: $formattedPrice",
                    style = MaterialTheme.typography.s15.bold(),
                    color = PrimaryColor
                )

                Text(
                    text = stringResource(id = R.string.ride_distance_format, booking.distanceInKm),
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextSecondary
                )
            }
        }
    }
}
