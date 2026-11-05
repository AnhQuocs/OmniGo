package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12

@Composable
internal fun RideMiniMapPreview(
    etaMinutes: Int,
    distanceKm: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimen.HeightRideMiniMapPreview)
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .background(SurfaceVariantLight)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle background road network lines
            val gridStroke = Stroke(width = 10f, cap = StrokeCap.Round)
            drawLine(
                color = SurfaceLight,
                start = Offset(0f, h * 0.35f),
                end = Offset(w, h * 0.55f),
                strokeWidth = gridStroke.width
            )
            drawLine(
                color = SurfaceLight,
                start = Offset(w * 0.25f, 0f),
                end = Offset(w * 0.18f, h),
                strokeWidth = gridStroke.width
            )
            drawLine(
                color = SurfaceLight,
                start = Offset(w * 0.72f, 0f),
                end = Offset(w * 0.65f, h),
                strokeWidth = gridStroke.width
            )

            // Active blue route path
            val startPt = Offset(w * 0.22f, h * 0.42f)
            val midPt = Offset(w * 0.42f, h * 0.80f)
            val endPt = Offset(w * 0.76f, h * 0.45f)

            val routePath = Path().apply {
                moveTo(startPt.x, startPt.y)
                lineTo(midPt.x, midPt.y)
                lineTo(endPt.x, endPt.y)
            }
            drawPath(
                path = routePath,
                color = PrimaryColor,
                style = Stroke(
                    width = 9f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Origin dot under vehicle marker
            drawCircle(
                color = TextPrimary,
                radius = 7f,
                center = startPt
            )

            // Destination orange pin with white halo
            drawCircle(
                color = SurfaceLight,
                radius = 16f,
                center = endPt
            )
            drawCircle(
                color = SecondaryColor,
                radius = 11f,
                center = endPt
            )
        }

        // Driver Blue Pin Icon
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = Dimen.PaddingXXL, top = Dimen.PaddingS)
                .size(Dimen.SizeL)
                .clip(CircleShape)
                .background(PrimaryColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TwoWheeler,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(Dimen.SizeS2)
            )
        }

        // Dark ETA & Distance Tooltip Pill
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(AppShape.ShapeS))
                .background(TextPrimary)
                .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.activity_ride_map_eta_mins, etaMinutes),
                style = MaterialTheme.typography.s12.bold(),
                color = TextWhite
            )
            Text(
                text = stringResource(R.string.activity_ride_map_dist_km, distanceKm),
                style = MaterialTheme.typography.s10.normal(),
                color = OutlineVariantLight
            )
        }
    }
}

