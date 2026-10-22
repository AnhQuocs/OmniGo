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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer
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
fun FoodOrderOfferDialog(
    offer: DriverFoodOffer,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_food_offer_title),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppShape.ShapeM))
                            .background(if (countdownSeconds <= 5) ErrorColor.copy(alpha = 0.15f) else PrimaryContainer)
                            .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXXS),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${countdownSeconds}s",
                            style = MaterialTheme.typography.s14.bold(),
                            color = if (countdownSeconds <= 5) ErrorColor else PrimaryColor
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
                    color = if (countdownSeconds <= 5) ErrorColor else PrimaryColor,
                    trackColor = OutlineVariantLight.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Earnings & Payment Method Card
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
                                text = stringResource(id = R.string.driver_food_offer_fee),
                                style = MaterialTheme.typography.s12.medium(),
                                color = TextSecondary
                            )
                            Text(
                                text = String.format(Locale.US, "%,.0f ₫", offer.deliveryFee),
                                style = MaterialTheme.typography.s20.bold(),
                                color = PrimaryColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppShape.ShapeS))
                                .background(if (offer.isPaid) SuccessColor.copy(alpha = 0.15f) else ErrorColor.copy(alpha = 0.15f))
                                .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS)
                        ) {
                            Text(
                                text = if (offer.isPaid) {
                                    stringResource(id = R.string.driver_food_offer_paid_online)
                                } else {
                                    stringResource(id = R.string.driver_food_offer_collect_cash)
                                },
                                style = MaterialTheme.typography.s12.semiBold(),
                                color = if (offer.isPaid) SuccessColor else ErrorColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Pickup location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.driver_food_offer_pickup),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                        Text(
                            text = offer.restaurantName,
                            style = MaterialTheme.typography.s15.semiBold(),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (offer.restaurantAddress.isNotBlank()) {
                            Text(
                                text = offer.restaurantAddress,
                                style = MaterialTheme.typography.s13.normal(),
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Delivery location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SuccessColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.driver_food_offer_delivery),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                        Text(
                            text = offer.dropOffAddress,
                            style = MaterialTheme.typography.s14.medium(),
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (offer.totalPrice > 0.0) {
                    Spacer(modifier = Modifier.height(AppSpacing.S))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.driver_food_offer_total),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "%,.0f ₫", offer.totalPrice),
                            style = MaterialTheme.typography.s13.medium(),
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Buttons: Reject & Accept
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isAccepting && !isRejecting,
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimen.HeightDefault)
                    ) {
                        if (isRejecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimen.SizeM),
                                strokeWidth = Dimen.PaddingXXS
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.driver_food_offer_reject),
                                style = MaterialTheme.typography.s14.semiBold()
                            )
                        }
                    }

                    Button(
                        onClick = onAccept,
                        enabled = !isAccepting && !isRejecting,
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor,
                            contentColor = OnPrimaryColor
                        ),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(Dimen.HeightDefault)
                    ) {
                        if (isAccepting) {
                            CircularProgressIndicator(
                                color = OnPrimaryColor,
                                modifier = Modifier.size(Dimen.SizeM),
                                strokeWidth = Dimen.PaddingXXS
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.driver_food_offer_accept),
                                style = MaterialTheme.typography.s14.bold()
                            )
                        }
                    }
                }
            }
        }
    }
}
