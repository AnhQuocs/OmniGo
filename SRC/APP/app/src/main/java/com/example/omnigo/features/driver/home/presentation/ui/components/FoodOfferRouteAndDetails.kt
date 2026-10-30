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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
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
fun FoodOfferSummaryCard(offer: DriverFoodOffer) {
    val formattedDeliveryFee = String.format(Locale.US, "%,.0f ₫", offer.deliveryFee)
    val formattedTotalPrice = String.format(Locale.US, "%,.0f ₫", offer.totalPrice)
    val isCash = !offer.isPaid

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
                    text = stringResource(id = R.string.driver_food_offer_fee),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary
                )
                Text(
                    text = formattedDeliveryFee,
                    style = MaterialTheme.typography.s24.bold(),
                    color = SuccessColor
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.PillShape))
                    .background(
                        if (isCash) SecondaryContainer else SuccessColor.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS)
            ) {
                Text(
                    text = if (offer.isPaid) {
                        stringResource(id = R.string.driver_food_offer_paid_online)
                    } else {
                        stringResource(id = R.string.driver_food_offer_collect_cash)
                    },
                    style = MaterialTheme.typography.s12.bold(),
                    color = if (offer.isPaid) SuccessColor else SecondaryVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.XS))

        Text(
            text = if (offer.isPaid) {
                stringResource(id = R.string.driver_food_offer_no_cash_note)
            } else {
                stringResource(id = R.string.driver_food_offer_cash_to_collect_note, formattedTotalPrice)
            },
            style = MaterialTheme.typography.s12.semiBold(),
            color = if (offer.isPaid) PrimaryColor else SecondaryVariant
        )
    }
}

@Composable
fun FoodOfferCustomerInfoRow(offer: DriverFoodOffer) {
    val displayName = offer.customerName?.trim()?.takeIf { it.isNotBlank() }
        ?: if (offer.customerId != null && offer.customerId > 0L) {
            "${stringResource(id = R.string.driver_trip_customer_name_default)} #${offer.customerId}"
        } else {
            stringResource(id = R.string.driver_food_offer_customer_default)
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
                    .background(SecondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = SecondaryVariant,
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
                .background(SecondaryVariant.copy(alpha = 0.12f))
                .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${offer.orderId}",
                style = MaterialTheme.typography.s13.bold(),
                color = SecondaryVariant
            )
        }
    }
}

@Composable
fun FoodOfferRouteCard(offer: DriverFoodOffer) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeL))
            .background(SurfaceVariantLight)
            .padding(Dimen.PaddingM)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeM)
                    .clip(CircleShape)
                    .background(PrimaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeS)
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.SPlus))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.driver_food_offer_restaurant_label),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary
                )
                Text(
                    text = offer.restaurantName,
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (offer.restaurantAddress.isNotBlank()) {
                    Text(
                        text = offer.restaurantAddress,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(start = Dimen.PaddingS, top = Dimen.PaddingXXS, bottom = Dimen.PaddingXXS)
                .width(Dimen.PaddingXXS)
                .height(Dimen.PaddingM)
                .background(OutlineVariantLight)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeM)
                    .clip(CircleShape)
                    .background(SuccessColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SuccessColor,
                    modifier = Modifier.size(Dimen.SizeS)
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.SPlus))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.driver_food_offer_delivery),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary
                )
                Text(
                    text = offer.dropOffAddress,
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
