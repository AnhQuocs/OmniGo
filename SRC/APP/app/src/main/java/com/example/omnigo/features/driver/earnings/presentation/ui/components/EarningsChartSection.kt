package com.example.omnigo.features.driver.earnings.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.omnigo.R
import com.example.omnigo.features.driver.earnings.domain.model.EarningsPeriod
import com.example.omnigo.features.driver.earnings.domain.util.EarningsChartPoint
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun EarningsChartSection(
    selectedPeriod: EarningsPeriod,
    chartPoints: List<EarningsChartPoint>,
    modifier: Modifier = Modifier
) {
    val periodLabelRes = when (selectedPeriod) {
        EarningsPeriod.TODAY -> R.string.driver_earnings_period_today
        EarningsPeriod.THIS_WEEK -> R.string.driver_earnings_period_week
        EarningsPeriod.THIS_MONTH -> R.string.driver_earnings_period_month
    }

    val hasData = chartPoints.any { it.amount > 0.0 }

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXS),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            // Header Row: Square pastel badge icon + Title + Dropdown Pill Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                ) {
                    // Rounded square pastel badge
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeWalletBadge)
                            .clip(RoundedCornerShape(AppShape.ShapeM))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.driver_earnings_chart_title),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }

                // Dropdown Pill Filter: "Hôm nay ∨"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.PillShape))
                        .border(
                            width = Dimen.BorderHairline,
                            color = CardBorderColor,
                            shape = RoundedCornerShape(AppShape.PillShape)
                        )
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.XXS)
                ) {
                    Text(
                        text = stringResource(id = periodLabelRes),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeIconXS)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Chart Frame: Y-Axis Labels + Dashed Grid Canvas + Bars / Watermark + X-Axis Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightEarningsChartContainer)
            ) {
                // Left Y-Axis Scale (300K, 200K, 100K, 0)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = Dimen.PaddingS),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    listOf("300K", "200K", "100K", "0").forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.s10.medium(),
                            color = TextTertiary
                        )
                    }
                }

                // Chart Grid & Content Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Layer 1: Dashed Grid Lines Canvas
                    val gridColor = OutlineVariantLight.copy(alpha = 0.55f)
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // 4 Horizontal Dashed Guideline Rows
                        for (i in 0..3) {
                            val y = (canvasHeight / 3f) * i
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.5f,
                                pathEffect = pathEffect
                            )
                        }

                        // Vertical Dashed Guideline Columns
                        val colCount = (chartPoints.size - 1).coerceAtLeast(1)
                        for (i in 0..colCount) {
                            val x = (canvasWidth / colCount.toFloat()) * i
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, canvasHeight),
                                strokeWidth = 1.5f,
                                pathEffect = pathEffect
                            )
                        }
                    }

                    // Layer 2: Bars or Empty State Overlay
                    if (hasData) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Dimen.PaddingXS),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            chartPoints.forEach { point ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (point.ratio > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .width(Dimen.PaddingSM)
                                                .fillMaxHeight(point.ratio)
                                                .clip(RoundedCornerShape(topStart = AppShape.ShapeXS, topEnd = AppShape.ShapeXS))
                                                .background(PrimaryColor)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty State Watermark: 3D translucent badge with watermark bars
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = Dimen.PaddingM),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(Dimen.SizeWalletBadge)
                                    .clip(CircleShape)
                                    .background(PrimaryContainer.copy(alpha = 0.55f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = PrimaryColor.copy(alpha = 0.45f),
                                    modifier = Modifier.size(Dimen.SizeSM)
                                )
                            }

                            Spacer(modifier = Modifier.height(AppSpacing.S))

                            Text(
                                text = stringResource(id = R.string.driver_earnings_chart_empty_title),
                                style = MaterialTheme.typography.s13.semiBold(),
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.XXS))

                            Text(
                                text = stringResource(id = R.string.driver_earnings_chart_empty_subtitle),
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextTertiary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            // Bottom X-Axis Timeline Scale (Aligned perfectly with grid columns)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimen.PaddingML), // offset matching the Y-axis label column
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chartPoints.forEach { point ->
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.s10.medium(),
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
