package com.example.omnigo.features.driver.trips.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

private const val TAB_ANIMATION_DURATION_MS = 250

@Composable
fun DriverPillTabSwitcher(
    selectedTabIndex: Int,
    activeCount: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeLabel = stringResource(id = R.string.driver_trips_tab_active_fmt, activeCount)
    val historyLabel = stringResource(id = R.string.driver_trips_tab_history)

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
            PillTabItem(
                title = activeLabel,
                icon = Icons.Default.DirectionsCar,
                isSelected = selectedTabIndex == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )

            PillTabItem(
                title = historyLabel,
                icon = Icons.AutoMirrored.Filled.Assignment,
                isSelected = selectedTabIndex == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PillTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryColor else TextSecondary,
        animationSpec = tween(durationMillis = TAB_ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
        label = "TabContentColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextSecondary,
        animationSpec = tween(durationMillis = TAB_ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
        label = "TabTextColor"
    )

    val itemModifier = if (isSelected) {
        modifier
            .shadow(
                elevation = Dimen.PaddingXS,
                shape = RoundedCornerShape(AppShape.PillShape),
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(AppShape.PillShape))
            .background(SurfaceLight)
    } else {
        modifier
            .clip(RoundedCornerShape(AppShape.PillShape))
            .background(Color.Transparent)
    }

    Box(
        modifier = itemModifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = Dimen.PaddingSM),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(Dimen.SizeS)
            )

            Spacer(modifier = Modifier.width(AppSpacing.S))

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
}

