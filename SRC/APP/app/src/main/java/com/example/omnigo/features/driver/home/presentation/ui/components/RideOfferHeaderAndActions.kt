package com.example.omnigo.features.driver.home.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun RideOfferHeader(
    countdownSeconds: Int,
    countdownProgress: Float
) {
    val isUrgent = countdownSeconds <= 5
    val badgeColor by animateColorAsState(
        targetValue = if (isUrgent) ErrorColor else PrimaryColor,
        label = "BadgeColor"
    )
    val badgeBg = if (isUrgent) ErrorContainer else PrimaryContainer

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.PillShape))
                    .background(PrimaryContainer)
                    .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeS)
                )
                Spacer(modifier = Modifier.width(AppSpacing.XS))
                Text(
                    text = stringResource(id = R.string.driver_ride_offer_service_ride),
                    style = MaterialTheme.typography.s12.bold(),
                    color = PrimaryColor
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.PillShape))
                    .background(badgeBg)
                    .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(Dimen.SizeS)
                )
                Spacer(modifier = Modifier.width(AppSpacing.XXS))
                Text(
                    text = "${countdownSeconds}s",
                    style = MaterialTheme.typography.s13.bold(),
                    color = badgeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.S))

        Text(
            text = stringResource(id = R.string.driver_ride_offer_new_subtitle),
            style = MaterialTheme.typography.s18.bold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        LinearProgressIndicator(
            progress = { countdownProgress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.PaddingXS)
                .clip(RoundedCornerShape(AppShape.ShapeXS)),
            color = badgeColor,
            trackColor = OutlineVariantLight.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun RideOfferActionButtons(
    countdownSeconds: Int,
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
                .height(Dimen.HeightDefault),
            shape = RoundedCornerShape(AppShape.ShapeL),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
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
                .weight(1.6f)
                .height(Dimen.HeightDefault),
            shape = RoundedCornerShape(AppShape.ShapeL),
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
                    text = "${stringResource(id = R.string.driver_ride_offer_accept)} ($countdownSeconds)",
                    style = MaterialTheme.typography.s15.bold()
                )
            }
        }
    }
}
