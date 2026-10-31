package com.example.omnigo.features.driver.common.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.DisabledButtonBackground
import com.example.omnigo.ui.theme.DisabledButtonContent
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s16
import kotlin.math.roundToInt

@Composable
fun DriverSwipeButton(
    text: String,
    onSwipeConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    trackColor: Color = PrimaryColor,
    thumbColor: Color = SurfaceLight,
    contentColor: Color = OnPrimaryColor,
    thumbIcon: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    thumbIconTint: Color? = null
) {
    val density = LocalDensity.current
    val thumbSize = Dimen.SizeXLPlus
    val paddingSize = Dimen.PaddingXS
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val paddingPx = with(density) { paddingSize.toPx() }

    var containerWidthPx by remember { mutableIntStateOf(0) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }

    val maxDragPx = (containerWidthPx - thumbSizePx - paddingPx * 2f).coerceAtLeast(1f)

    LaunchedEffect(text, isLoading, enabled) {
        if (!isLoading) {
            dragOffsetPx = 0f
        }
    }

    val animatedOffsetPx by animateFloatAsState(
        targetValue = if (isLoading) maxDragPx else dragOffsetPx,
        animationSpec = tween(durationMillis = 150),
        label = "swipeThumbOffset"
    )

    val effectiveTrackColor = if (enabled) trackColor else DisabledButtonBackground
    val effectiveContentColor = if (enabled) contentColor else DisabledButtonContent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimen.SizeXXL)
            .clip(RoundedCornerShape(AppShape.PillShape))
            .background(effectiveTrackColor)
            .onSizeChanged { containerWidthPx = it.width }
            .padding(paddingSize),
        contentAlignment = Alignment.CenterStart
    ) {
        // Progress fill behind thumb
        val progressFraction = (animatedOffsetPx / maxDragPx).coerceIn(0f, 1f)
        if (progressFraction > 0.02f && enabled) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progressFraction)
                    .clip(RoundedCornerShape(AppShape.PillShape))
                    .background(SurfaceLight.copy(alpha = 0.2f))
            )
        }

        // Center label
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.s16.bold(),
                color = effectiveContentColor.copy(alpha = (1f - progressFraction * 0.7f).coerceIn(0.2f, 1f)),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = Dimen.SizeL)
            )
        }

        // Draggable thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetPx.roundToInt(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(if (enabled) thumbColor else DisabledButtonContent.copy(alpha = 0.3f))
                .pointerInput(enabled, isLoading, maxDragPx) {
                    if (!enabled || isLoading) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragOffsetPx >= maxDragPx * 0.75f) {
                                dragOffsetPx = maxDragPx
                                onSwipeConfirm()
                                dragOffsetPx = 0f
                            } else {
                                dragOffsetPx = 0f
                            }
                        },
                        onDragCancel = {
                            dragOffsetPx = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetPx = (dragOffsetPx + dragAmount).coerceIn(0f, maxDragPx)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.SizeM),
                    color = trackColor,
                    strokeWidth = Dimen.PaddingXXS
                )
            } else {
                Icon(
                    imageVector = thumbIcon,
                    contentDescription = text,
                    tint = thumbIconTint ?: if (enabled) trackColor else DisabledButtonContent,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            }
        }
    }
}
