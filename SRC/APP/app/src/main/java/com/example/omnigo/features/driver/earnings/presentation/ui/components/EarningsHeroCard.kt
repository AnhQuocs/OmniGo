package com.example.omnigo.features.driver.earnings.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.omnigo.R
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.features.driver.earnings.domain.model.EarningsPeriod
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.HeroGlassBackground
import com.example.omnigo.ui.theme.HeroGlassBorder
import com.example.omnigo.ui.theme.HeroGradientEnd
import com.example.omnigo.ui.theme.HeroGradientMiddle
import com.example.omnigo.ui.theme.HeroGradientStart
import com.example.omnigo.ui.theme.HeroWalletGlowCenter
import com.example.omnigo.ui.theme.HeroWalletGlowEdge
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s32
import java.util.Locale

@Composable
fun EarningsHeroCard(
    report: DriverDailyReport?,
    period: EarningsPeriod,
    modifier: Modifier = Modifier
) {
    val totalRevenue = report?.totalRevenue ?: 0.0
    val netEarnings = report?.driverEarnings ?: 0.0
    val platformFee = report?.platformFee ?: 0.0
    val totalTrips = report?.totalTrips ?: 0

    val periodTitleRes = when (period) {
        EarningsPeriod.TODAY -> R.string.driver_earnings_total_today
        EarningsPeriod.THIS_WEEK -> R.string.driver_earnings_total_week
        EarningsPeriod.THIS_MONTH -> R.string.driver_earnings_total_month
    }

    Card(
        shape = RoundedCornerShape(AppShape.ShapeXL2),
        colors = CardDefaults.cardColors(containerColor = PrimaryColor),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXS),
        modifier = modifier.fillMaxWidth()
    ) {
        // Parent Container (Layer 1: Gradient Base)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            HeroGradientStart,
                            HeroGradientMiddle,
                            HeroGradientEnd
                        )
                    )
                )
                .padding(Dimen.PaddingSM)
        ) {
            // LAYER 2A: Radiant Glow behind the 3D wallet
            Box(
                modifier = Modifier
                    .size(Dimen.SizeWalletGlow)
                    .align(Alignment.TopEnd)
                    .offset(x = Dimen.PaddingS, y = -Dimen.PaddingXS)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                HeroWalletGlowCenter,
                                HeroWalletGlowEdge,
                                Color.Transparent
                            )
                        )
                    )
            )

            // LAYER 2B: 3D Wallet Illustration placed independently on top-right
            Image(
                painter = painterResource(id = R.drawable.ic_income_wallet),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(Dimen.SizeHeroWalletIllustration)
                    .align(Alignment.TopEnd)
                    .offset(x = Dimen.PaddingS, y = -Dimen.PaddingXXS)
            )

            // LAYER 3: 2 Glassmorphic Content Boxes stacked vertically over the background
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Glass Box: Contains Title, Trips Badge, and Large Amount (Glass overlay over wallet)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppShape.ShapeL))
                        .background(HeroGlassBackground)
                        .border(
                            width = Dimen.BorderHairline,
                            color = HeroGlassBorder,
                            shape = RoundedCornerShape(AppShape.ShapeL)
                        )
                        .padding(Dimen.PaddingSM)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header Row: Title & Trips Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = periodTitleRes),
                                style = MaterialTheme.typography.s14.medium(),
                                color = OnPrimaryColor.copy(alpha = 0.95f)
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AppShape.PillShape))
                                    .background(OnPrimaryColor.copy(alpha = 0.20f))
                                    .border(
                                        width = Dimen.BorderHairline,
                                        color = OnPrimaryColor.copy(alpha = 0.30f),
                                        shape = RoundedCornerShape(AppShape.PillShape)
                                    )
                                    .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.XXS)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = OnPrimaryColor,
                                    modifier = Modifier.size(Dimen.SizeIconXS)
                                )
                                Text(
                                    text = stringResource(id = R.string.driver_earnings_trips_count_fmt, totalTrips),
                                    style = MaterialTheme.typography.s12.medium(),
                                    color = OnPrimaryColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.S))

                        // Large Total Income Amount
                        Text(
                            text = String.format(Locale.getDefault(), "%,.0f ₫", totalRevenue),
                            style = MaterialTheme.typography.s32.bold(),
                            color = OnPrimaryColor
                        )
                    }
                }

                // Tight Spacer between Top Glass Box and Bottom Glass Box
                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Bottom Glass Box: Contains the 3 Breakdown Fees
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppShape.ShapeL))
                        .background(HeroGlassBackground)
                        .border(
                            width = Dimen.BorderHairline,
                            color = HeroGlassBorder,
                            shape = RoundedCornerShape(AppShape.ShapeL)
                        )
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingS)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeroFeeColumn(
                            label = stringResource(id = R.string.driver_earnings_net_received_label),
                            value = String.format(Locale.getDefault(), "%,.0f ₫", netEarnings),
                            modifier = Modifier.weight(1f)
                        )

                        HeroFeeColumn(
                            label = stringResource(id = R.string.driver_earnings_platform_fee_label),
                            value = String.format(Locale.getDefault(), "%,.0f ₫", platformFee),
                            modifier = Modifier.weight(1f)
                        )

                        HeroFeeColumn(
                            label = stringResource(id = R.string.driver_earnings_gross_fare_label),
                            value = String.format(Locale.getDefault(), "%,.0f ₫", totalRevenue),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroFeeColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = Dimen.PaddingXXS),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.s10.medium(),
            color = OnPrimaryColor.copy(alpha = 0.88f),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(AppSpacing.XXS))
        Text(
            text = value,
            style = MaterialTheme.typography.s14.bold(),
            color = OnPrimaryColor,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
