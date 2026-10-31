package com.example.omnigo.features.driver.home.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OnSurfaceVariantLight
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12

@Composable
fun DriverStatusPill(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val dotColor by animateColorAsState(
        targetValue = if (isOnline) SuccessColor else OnSurfaceVariantLight,
        animationSpec = tween(durationMillis = 300),
        label = "statusDotColor"
    )

    // Pure elevated Box (NO Card/Surface tint, pure crisp SurfaceLight white)
    Box(
        modifier = modifier
            .shadow(
                elevation = Dimen.PaddingS,
                shape = CircleShape,
                clip = false
            )
            .clip(CircleShape)
            .background(SurfaceLight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXSPlus),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.PaddingS)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = stringResource(
                    id = if (isOnline) R.string.driver_home_pill_online else R.string.driver_home_pill_offline
                ),
                style = MaterialTheme.typography.s12.medium(),
                color = TextPrimary
            )
        }
    }
}
