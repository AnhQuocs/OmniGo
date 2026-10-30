package com.example.omnigo.features.driver.home.presentation.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.driver.domain.model.RideOfferEvent
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SecondaryVariant
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s24
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun RideOfferSummaryCard(offer: RideOfferEvent) {
    val formattedFare = String.format(Locale.US, "%,.0f ₫", offer.price)
    val isCash = offer.paymentMethod == null || offer.paymentMethod.equals("CASH", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeL))
            .background(SurfaceVariantLight)
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.driver_ride_offer_fare),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary
                )
                Text(
                    text = formattedFare,
                    style = MaterialTheme.typography.s24.bold(),
                    color = PrimaryColor
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.PillShape))
                    .background(if (isCash) SecondaryContainer else SuccessColor.copy(alpha = 0.15f))
                    .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS)
            ) {
                Text(
                    text = stringResource(
                        id = if (isCash) R.string.driver_ride_offer_payment_cash else R.string.driver_ride_offer_payment_online
                    ),
                    style = MaterialTheme.typography.s12.bold(),
                    color = if (isCash) SecondaryVariant else SuccessColor
                )
            }
        }
    }
}

@Composable
fun RideOfferCustomerInfoRow(offer: RideOfferEvent) {
    val displayName = offer.customerName?.trim()?.takeIf { it.isNotBlank() }
        ?: if (offer.customerId != null && offer.customerId > 0L) {
            "${stringResource(id = R.string.driver_trip_customer_name_default)} #${offer.customerId}"
        } else {
            stringResource(id = R.string.driver_ride_offer_customer_default)
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeL))
            .background(SurfaceVariantLight)
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeL)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeS2)
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.S))
            Column {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(id = R.string.driver_ride_offer_customer_label),
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(AppShape.ShapeS))
                .background(PrimaryColor.copy(alpha = 0.1f))
                .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Route,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeS)
            )
            Spacer(modifier = Modifier.width(AppSpacing.XS))
            Text(
                text = stringResource(id = R.string.ride_distance_format, offer.distanceInKm),
                style = MaterialTheme.typography.s13.bold(),
                color = PrimaryColor
            )
        }
    }
}

@Composable
fun RideOfferRouteCard(offer: RideOfferEvent) {
    val pickupDisplay = offer.pickupAddress.ifBlank {
        stringResource(id = R.string.driver_ride_offer_coordinates_format, offer.pickupLat, offer.pickupLng)
    }
    val dropoffDisplay = offer.dropoffAddress.ifBlank {
        stringResource(id = R.string.driver_ride_offer_coordinates_format, offer.dropoffLat, offer.dropoffLng)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeL))
            .background(SurfaceVariantLight)
            .padding(Dimen.PaddingM)
    ) {
        RideOfferRoutePointRow(
            icon = Icons.Default.LocationOn,
            tint = SuccessColor,
            label = stringResource(id = R.string.driver_ride_offer_pickup),
            address = pickupDisplay
        )

        Box(
            modifier = Modifier
                .padding(start = Dimen.PaddingS, top = Dimen.PaddingXXS, bottom = Dimen.PaddingXXS)
                .width(Dimen.PaddingXXS)
                .height(Dimen.PaddingM)
                .background(OutlineVariantLight)
        )

        RideOfferRoutePointRow(
            icon = Icons.Default.Navigation,
            tint = SecondaryColor,
            label = stringResource(id = R.string.driver_ride_offer_dropoff),
            address = dropoffDisplay
        )
    }
}

@Composable
private fun RideOfferRoutePointRow(
    icon: ImageVector,
    tint: Color,
    label: String,
    address: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(Dimen.SizeM)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(Dimen.SizeS)
            )
        }
        Spacer(modifier = Modifier.width(AppSpacing.SPlus))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.s12.medium(),
                color = TextSecondary
            )
            Text(
                text = address,
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
