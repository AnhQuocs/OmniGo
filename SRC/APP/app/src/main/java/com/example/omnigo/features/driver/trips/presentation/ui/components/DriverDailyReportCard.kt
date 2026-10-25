package com.example.omnigo.features.driver.trips.presentation.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DriverDailyReportCard(
    report: DriverDailyReport?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingL)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeXL)
                        .clip(CircleShape)
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }

                Spacer(modifier = Modifier.size(AppSpacing.M))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.driver_daily_report_title),
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary
                    )
                    if (report != null && !isLoading) {
                        Text(
                            text = stringResource(
                                id = R.string.driver_daily_report_trips_count_format,
                                report.totalTrips
                            ),
                            style = MaterialTheme.typography.s12.medium(),
                            color = PrimaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = CardBorderColor, thickness = Dimen.PaddingXXS)
            Spacer(modifier = Modifier.height(AppSpacing.M))

            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.PaddingM),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeM),
                        color = PrimaryColor,
                        strokeWidth = Dimen.PaddingXXS
                    )
                    Spacer(modifier = Modifier.size(AppSpacing.S))
                    Text(
                        text = stringResource(id = R.string.driver_trips_report_loading),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            } else {
                val safeReport = report ?: DriverDailyReport()
                val netEarningsFormatted = String.format(Locale.getDefault(), "%,.0f đ", safeReport.driverEarnings)
                val grossRevenueFormatted = String.format(Locale.getDefault(), "%,.0f đ", safeReport.totalRevenue)
                val platformFeeFormatted = String.format(Locale.getDefault(), "%,.0f đ", safeReport.platformFee)
                val cashFormatted = String.format(Locale.getDefault(), "%,.0f đ", safeReport.cashCollected)
                val appPaidFormatted = String.format(Locale.getDefault(), "%,.0f đ", safeReport.appPaid)

                // Highlight Net Earnings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_daily_report_net_earnings),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextSecondary
                    )
                    Text(
                        text = netEarningsFormatted,
                        style = MaterialTheme.typography.s20.bold(),
                        color = SuccessColor
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Gross Fare
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_daily_report_gross_revenue),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                    Text(
                        text = grossRevenueFormatted,
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                // Platform fee
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_daily_report_platform_fee),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                    Text(
                        text = "-$platformFeeFormatted",
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.SPlus))

                // Breakdown: Cash vs App
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_daily_report_cash, cashFormatted),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                    Text(
                        text = stringResource(id = R.string.driver_daily_report_app_paid, appPaidFormatted),
                        style = MaterialTheme.typography.s12.medium(),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
