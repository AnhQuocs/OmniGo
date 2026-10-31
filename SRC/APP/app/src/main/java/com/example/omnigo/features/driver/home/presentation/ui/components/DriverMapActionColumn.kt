package com.example.omnigo.features.driver.home.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary

@Composable
fun DriverMapActionColumn(
    onSupportClick: () -> Unit,
    onCompassClick: () -> Unit,
    onRecenterClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSupportActive: Boolean = false,
    isCompassActive: Boolean = false,
    isGpsCentered: Boolean = true,
    bearing: Float = 0f
) {
    val parentRadius = AppShape.ShapeXL
    val subtleRadius = AppShape.ShapeXXS

    // Top item: rounded top (matches parent shape), flat with subtle curve at bottom
    val topItemShape = remember {
        RoundedCornerShape(
            topStart = parentRadius,
            topEnd = parentRadius,
            bottomStart = subtleRadius,
            bottomEnd = subtleRadius
        )
    }
    // Middle item: flat 4 sides, subtle 4 corners
    val centerItemShape = remember {
        RoundedCornerShape(subtleRadius)
    }
    // Bottom item: flat with subtle curve at top, rounded bottom (matches parent shape)
    val bottomItemShape = remember {
        RoundedCornerShape(
            topStart = subtleRadius,
            topEnd = subtleRadius,
            bottomStart = parentRadius,
            bottomEnd = parentRadius
        )
    }

    // Pure elevated Box (NO Card/Surface tint, pure crisp SurfaceLight white)
    Box(
        modifier = modifier
            .width(Dimen.HeightDefault)
            .shadow(
                elevation = Dimen.PaddingS,
                shape = RoundedCornerShape(parentRadius),
                clip = false
            )
            .clip(RoundedCornerShape(parentRadius))
            .background(SurfaceLight)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Support 🎧 (Highlight when support sheet is active)
            RailActionItem(
                icon = Icons.Default.Headphones,
                contentDescription = stringResource(id = R.string.driver_home_support_tooltip),
                shape = topItemShape,
                isSelected = isSupportActive,
                onClick = onSupportClick
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingXS),
                thickness = Dimen.PaddingXXS,
                color = CardBorderColor.copy(alpha = 0.25f)
            )

            // 2. Compass 🧭 (Rotates with map bearing; highlight when bearing != 0)
            RailActionItem(
                icon = Icons.Outlined.Explore,
                contentDescription = stringResource(id = R.string.driver_home_compass_tooltip),
                shape = centerItemShape,
                isSelected = isCompassActive,
                rotationDegrees = -bearing,
                onClick = onCompassClick
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingXS),
                thickness = Dimen.PaddingXXS,
                color = CardBorderColor.copy(alpha = 0.25f)
            )

            // 3. Recenter GPS 📍 (Auto highlighted when centered on GPS)
            RailActionItem(
                icon = Icons.Default.LocationOn,
                contentDescription = stringResource(id = R.string.driver_cockpit_recenter_gps),
                shape = bottomItemShape,
                isSelected = isGpsCentered,
                onClick = onRecenterClick
            )
        }
    }
}

@Composable
private fun RailActionItem(
    icon: ImageVector,
    contentDescription: String,
    shape: Shape,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    rotationDegrees: Float = 0f,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "railItemBgColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) PrimaryColor else TextPrimary,
        animationSpec = tween(durationMillis = 200),
        label = "railItemIconTint"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimen.HeightDefault)
            .padding(Dimen.PaddingXXS)
            .clip(shape)
            .background(bgColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = PrimaryColor),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier
                .size(Dimen.SizeM)
                .rotate(rotationDegrees)
        )
    }
}
