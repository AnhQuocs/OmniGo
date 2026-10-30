package com.example.omnigo.features.driver.earnings.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.earnings.domain.model.EarningsPeriod
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun EarningsPeriodFilterChips(
    selectedPeriod: EarningsPeriod,
    onPeriodSelected: (EarningsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(
        EarningsPeriod.TODAY to R.string.driver_earnings_period_today,
        EarningsPeriod.THIS_WEEK to R.string.driver_earnings_period_week,
        EarningsPeriod.THIS_MONTH to R.string.driver_earnings_period_month
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
    ) {
        periods.forEach { (period, stringResId) ->
            val isSelected = selectedPeriod == period
            val backgroundColor = if (isSelected) PrimaryContainer else SurfaceLight
            val borderColor = if (isSelected) PrimaryColor else CardBorderColor
            val textColor = if (isSelected) PrimaryColor else TextSecondary

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppShape.ShapeL))
                    .background(backgroundColor)
                    .border(
                        width = Dimen.PaddingXXS,
                        color = borderColor,
                        shape = RoundedCornerShape(AppShape.ShapeL)
                    )
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = Dimen.PaddingSM),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = stringResId),
                    style = if (isSelected) {
                        MaterialTheme.typography.s14.semiBold()
                    } else {
                        MaterialTheme.typography.s14.normal()
                    },
                    color = textColor
                )
            }
        }
    }
}

