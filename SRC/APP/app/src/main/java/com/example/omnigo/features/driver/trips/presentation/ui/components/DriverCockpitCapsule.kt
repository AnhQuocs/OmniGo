package com.example.omnigo.features.driver.trips.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.RadarCyan
import com.example.omnigo.ui.theme.RadarCyanGlow
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s16

private const val RADAR_PULSE_DURATION_MS = 2200

@Composable
fun DriverCockpitCapsule(
    activeCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.PillShape),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = Dimen.PaddingXXS,
                shape = RoundedCornerShape(AppShape.PillShape),
                ambientColor = PrimaryColor.copy(alpha = 0.06f),
                spotColor = PrimaryColor.copy(alpha = 0.10f)
            )
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor.copy(alpha = 0.7f),
                shape = RoundedCornerShape(AppShape.PillShape)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radar Sóng Neon thon gọn
            CockpitRadarPulseIndicator(
                modifier = Modifier.size(Dimen.SizeXLPlus)
            )

            Spacer(modifier = Modifier.width(AppSpacing.S))

            // Thước đo 3 điểm cân xứng tuyệt đối
            CockpitGaugeTrack(
                activeCount = activeCount,
                completedCount = completedCount,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CockpitRadarPulseIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "RadarPulseTransition")

    val pulse1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(RADAR_PULSE_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse1"
    )

    val alpha1 by transition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(RADAR_PULSE_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Alpha1"
    )

    val pulse2 by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(RADAR_PULSE_DURATION_MS, delayMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse2"
    )

    val alpha2 by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(RADAR_PULSE_DURATION_MS, delayMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Alpha2"
    )

    Canvas(modifier = modifier) {
        val center = this.center
        val maxRadius = size.minDimension / 2f

        // Vòng sóng lan tỏa 1
        drawCircle(
            color = RadarCyan.copy(alpha = alpha1),
            radius = maxRadius * pulse1,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Vòng sóng lan tỏa 2
        drawCircle(
            color = RadarCyan.copy(alpha = alpha2),
            radius = maxRadius * pulse2,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Quầng sáng nền tĩnh
        drawCircle(
            color = RadarCyanGlow,
            radius = maxRadius * 0.42f,
            center = center
        )

        // Chấm radar tâm phát sáng màu Cyan Neon
        drawCircle(
            color = RadarCyan,
            radius = maxRadius * 0.24f,
            center = center
        )
    }
}

@Composable
private fun CockpitGaugeTrack(
    activeCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cột 1: Sẵn sàng
        CockpitGaugeNode(
            value = "0",
            label = stringResource(id = R.string.driver_trips_cockpit_ready),
            valueColor = RadarCyan,
            modifier = Modifier.weight(1f)
        )

        // Line nối 1
        Box(
            modifier = Modifier
                .weight(0.4f)
                .height(Dimen.PaddingXXS)
                .background(CardBorderColor)
        )

        // Cột 2: Đang chạy
        CockpitGaugeNode(
            value = activeCount.toString(),
            label = stringResource(id = R.string.driver_trips_cockpit_active),
            valueColor = if (activeCount > 0) PrimaryColor else TextPrimary,
            modifier = Modifier.weight(1f)
        )

        // Line nối 2
        Box(
            modifier = Modifier
                .weight(0.4f)
                .height(Dimen.PaddingXXS)
                .background(CardBorderColor)
        )

        // Cột 3: Hoàn thành
        CockpitGaugeNode(
            value = completedCount.toString(),
            label = stringResource(id = R.string.driver_trips_cockpit_completed),
            valueColor = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CockpitGaugeNode(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.s16.bold(),
            color = valueColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.s10.normal(),
            color = TextSecondary,
            textAlign = TextAlign.Center,
            softWrap = false
        )
    }
}
