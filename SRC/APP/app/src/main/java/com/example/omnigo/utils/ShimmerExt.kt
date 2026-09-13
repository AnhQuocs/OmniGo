package com.example.omnigo.utils

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import com.example.omnigo.ui.theme.ShimmerBase
import com.example.omnigo.ui.theme.ShimmerHighlight

@Composable
fun rememberShimmerBrush(
    baseColor: Color = ShimmerBase,
    highlightColor: Color = ShimmerHighlight,
    durationMillis: Int = 1200
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        ),
        start = Offset(x = translateAnimation.value - 300f, y = translateAnimation.value - 300f),
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
}

fun Modifier.shimmerBackground(
    brush: Brush,
    shape: Shape = RectangleShape
): Modifier = this.background(brush = brush, shape = shape)
