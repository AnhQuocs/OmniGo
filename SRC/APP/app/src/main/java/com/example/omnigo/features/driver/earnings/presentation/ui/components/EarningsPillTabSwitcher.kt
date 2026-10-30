package com.example.omnigo.features.driver.earnings.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.earnings.domain.model.EarningsPeriod
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

private const val TAB_SWITCH_DURATION_MS = 250

@Composable
fun EarningsPillTabSwitcher(
    selectedPeriod: EarningsPeriod,
    onPeriodSelected: (EarningsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(
        EarningsPeriod.TODAY to R.string.driver_earnings_period_today,
        EarningsPeriod.THIS_WEEK to R.string.driver_earnings_period_week,
        EarningsPeriod.THIS_MONTH to R.string.driver_earnings_period_month
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.PillShape))
            .background(SurfaceVariantLight)
            .padding(Dimen.PaddingXXS)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.XXS)
        ) {
            periods.forEach { (period, stringResId) ->
                val isSelected = selectedPeriod == period
                EarningsPillTabItem(
                    title = stringResource(id = stringResId),
                    isSelected = isSelected,
                    onClick = { onPeriodSelected(period) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EarningsPillTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryColor else Color.Transparent,
        animationSpec = tween(durationMillis = TAB_SWITCH_DURATION_MS, easing = FastOutSlowInEasing),
        label = "EarningsTabBgColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) OnPrimaryColor else TextSecondary,
        animationSpec = tween(durationMillis = TAB_SWITCH_DURATION_MS, easing = FastOutSlowInEasing),
        label = "EarningsTabTextColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppShape.PillShape))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = Dimen.PaddingSM),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = if (isSelected) {
                MaterialTheme.typography.s14.semiBold()
            } else {
                MaterialTheme.typography.s14.normal()
            },
            color = textColor,
            maxLines = 1
        )
    }
}

