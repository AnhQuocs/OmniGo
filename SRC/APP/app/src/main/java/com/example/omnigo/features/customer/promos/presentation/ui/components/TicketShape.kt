package com.example.omnigo.features.customer.promos.presentation.ui.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class TicketShape(
    private val cornerRadius: Dp = 16.dp,
    private val notchRadius: Dp = 10.dp,
    private val notchPositionX: Dp = 96.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerPx = with(density) { cornerRadius.toPx() }
        val notchRPx = with(density) { notchRadius.toPx() }
        val notchXPx = with(density) { notchPositionX.toPx() }
            .coerceIn(cornerPx + notchRPx, size.width - cornerPx - notchRPx)

        val path = Path().apply {
            reset()
            // 1. Top-left corner
            moveTo(0f, cornerPx)
            arcTo(
                rect = Rect(0f, 0f, 2 * cornerPx, 2 * cornerPx),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // 2. Top edge to notch
            lineTo(notchXPx - notchRPx, 0f)
            // 3. Top notch (curves downward into ticket)
            arcTo(
                rect = Rect(notchXPx - notchRPx, -notchRPx, notchXPx + notchRPx, notchRPx),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            // 4. Top edge to top-right corner
            lineTo(size.width - cornerPx, 0f)
            // 5. Top-right corner
            arcTo(
                rect = Rect(size.width - 2 * cornerPx, 0f, size.width, 2 * cornerPx),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // 6. Right edge
            lineTo(size.width, size.height - cornerPx)
            // 7. Bottom-right corner
            arcTo(
                rect = Rect(size.width - 2 * cornerPx, size.height - 2 * cornerPx, size.width, size.height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // 8. Bottom edge to bottom notch
            lineTo(notchXPx + notchRPx, size.height)
            // 9. Bottom notch (curves upward into ticket)
            arcTo(
                rect = Rect(notchXPx - notchRPx, size.height - notchRPx, notchXPx + notchRPx, size.height + notchRPx),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            // 10. Bottom edge to bottom-left corner
            lineTo(cornerPx, size.height)
            // 11. Bottom-left corner
            arcTo(
                rect = Rect(0f, size.height - 2 * cornerPx, 2 * cornerPx, size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // 12. Left edge
            lineTo(0f, cornerPx)
            close()
        }
        return Outline.Generic(path)
    }
}

