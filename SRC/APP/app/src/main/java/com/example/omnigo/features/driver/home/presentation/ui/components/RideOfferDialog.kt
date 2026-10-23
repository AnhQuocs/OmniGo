package com.example.omnigo.features.driver.home.presentation.ui.components

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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.omnigo.R
import com.example.omnigo.features.driver.domain.model.RideOfferEvent
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun RideOfferDialog(
    offer: RideOfferEvent,
    countdownSeconds: Int,
    countdownProgress: Float,
    isAccepting: Boolean,
    isRejecting: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = { /* Prevent dismiss on outside touch */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
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
                    .padding(Dimen.PaddingL)
            ) {
                // Header with countdown
                RideOfferHeader(
                    countdownSeconds = countdownSeconds,
                    countdownProgress = countdownProgress
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Fare & Details Card
                RideOfferSummaryCard(offer = offer)

                Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

                // Route: Pickup & Dropoff
                RideOfferRouteCard(offer = offer)

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Actions: Reject and Accept
                RideOfferActionButtons(
                    isAccepting = isAccepting,
                    isRejecting = isRejecting,
                    onAccept = onAccept,
                    onReject = onReject
                )
            }
        }
    }
}

@Composable
private fun RideOfferHeader(
    countdownSeconds: Int,
    countdownProgress: Float
) {
    val isUrgent = countdownSeconds <= 5
    val highlightColor = if (isUrgent) ErrorColor else PrimaryColor

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.driver_ride_offer_title),
            style = MaterialTheme.typography.s18.bold(),
            color = TextPrimary
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(AppShape.ShapeM))
                .background(if (isUrgent) ErrorColor.copy(alpha = 0.15f) else PrimaryContainer)
                .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXXS),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${countdownSeconds}s",
                style = MaterialTheme.typography.s14.bold(),
                color = highlightColor
            )
        }
    }

    Spacer(modifier = Modifier.height(AppSpacing.S))

    LinearProgressIndicator(
        progress = { countdownProgress.coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimen.PaddingXS)
            .clip(RoundedCornerShape(AppShape.ShapeXS)),
        color = highlightColor,
        trackColor = OutlineVariantLight.copy(alpha = 0.3f)
    )
}

@Composable
private fun RideOfferSummaryCard(offer: RideOfferEvent) {
    val formattedFare = String.format(Locale.getDefault(), "%,.0f đ", offer.price)
    val isCash = offer.paymentMethod == null || offer.paymentMethod.equals("CASH", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .background(PrimaryContainer.copy(alpha = 0.4f))
            .border(
                width = Dimen.PaddingXXS,
                color = PrimaryColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(AppShape.ShapeM)
            )
            .padding(Dimen.PaddingM)
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
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = formattedFare,
                    style = MaterialTheme.typography.s20.bold(),
                    color = PrimaryColor
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeS))
                        .background(if (isCash) SuccessColor.copy(alpha = 0.15f) else PrimaryColor.copy(alpha = 0.15f))
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                ) {
                    Text(
                        text = stringResource(
                            id = if (isCash) R.string.driver_ride_offer_payment_cash else R.string.driver_ride_offer_payment_online
                        ),
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = if (isCash) SuccessColor else PrimaryColor
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                Text(
                    text = stringResource(id = R.string.ride_distance_format, offer.distanceInKm),
                    style = MaterialTheme.typography.s13.medium(),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun RideOfferRouteCard(offer: RideOfferEvent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .background(SurfaceLight)
            .border(
                width = Dimen.PaddingXXS,
                color = OutlineVariantLight.copy(alpha = 0.5f),
                shape = RoundedCornerShape(AppShape.ShapeM)
            )
            .padding(Dimen.PaddingM)
    ) {
        // Pickup
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
                    modifier = Modifier.size(Dimen.SizeSM)
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.driver_ride_offer_pickup),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary
                )
                Text(
                    text = stringResource(
                        id = R.string.driver_ride_offer_coordinates_format,
                        offer.pickupLat,
                        offer.pickupLng
                    ),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Connecting indicator
        Box(
            modifier = Modifier
                .padding(start = Dimen.PaddingSM)
                .width(Dimen.PaddingXXS)
                .height(Dimen.PaddingL)
                .background(OutlineVariantLight)
        )

        // Dropoff
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeM)
                    .clip(CircleShape)
                    .background(ErrorColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = ErrorColor,
                    modifier = Modifier.size(Dimen.SizeSM)
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.driver_ride_offer_dropoff),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary
                )
                Text(
                    text = stringResource(
                        id = R.string.driver_ride_offer_coordinates_format,
                        offer.dropoffLat,
                        offer.dropoffLng
                    ),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RideOfferActionButtons(
    isAccepting: Boolean,
    isRejecting: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
    ) {
        OutlinedButton(
            onClick = onReject,
            enabled = !isAccepting && !isRejecting,
            modifier = Modifier
                .weight(1f)
                .height(Dimen.SizeMega),
            shape = RoundedCornerShape(AppShape.ShapeM),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextSecondary
            )
        ) {
            if (isRejecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.SizeSM),
                    color = TextSecondary,
                    strokeWidth = Dimen.PaddingXXS
                )
            } else {
                Text(
                    text = stringResource(id = R.string.driver_ride_offer_reject),
                    style = MaterialTheme.typography.s14.semiBold()
                )
            }
        }

        Button(
            onClick = onAccept,
            enabled = !isAccepting && !isRejecting,
            modifier = Modifier
                .weight(1.5f)
                .height(Dimen.SizeMega),
            shape = RoundedCornerShape(AppShape.ShapeM),
            colors = ButtonDefaults.buttonColors(
                containerColor = SuccessColor,
                contentColor = OnPrimaryColor
            )
        ) {
            if (isAccepting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.SizeSM),
                    color = OnPrimaryColor,
                    strokeWidth = Dimen.PaddingXXS
                )
            } else {
                Text(
                    text = stringResource(id = R.string.driver_ride_offer_accept),
                    style = MaterialTheme.typography.s15.bold()
                )
            }
        }
    }
}
