package com.example.omnigo.features.driver.ride.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DriverTripSettlementDialog(
    totalFare: Double,
    platformFee: Double,
    driverEarnings: Double,
    isCashPayment: Boolean,
    onDismissAndFinish: () -> Unit
) {
    val formattedTotal = String.format(Locale.US, "%,.0f đ", totalFare)
    val formattedFee = String.format(Locale.US, "- %,.0f đ", platformFee)
    val formattedEarnings = String.format(Locale.US, "%,.0f đ", driverEarnings)

    AlertDialog(
        onDismissRequest = onDismissAndFinish,
        shape = RoundedCornerShape(AppShape.ShapeXL),
        containerColor = SurfaceLight,
        title = {
            Text(
                text = stringResource(R.string.driver_trip_settlement_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Total Fare
                SettlementRow(
                    label = stringResource(R.string.driver_trip_settlement_total_fare),
                    value = formattedTotal,
                    isHighlight = false
                )

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Platform fee
                SettlementRow(
                    label = stringResource(R.string.driver_trip_settlement_platform_fee),
                    value = formattedFee,
                    isHighlight = false,
                    valueColor = TextSecondary
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))
                HorizontalDivider(color = CardBorderColor, thickness = Dimen.PaddingXXS)
                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Net earnings
                SettlementRow(
                    label = stringResource(R.string.driver_trip_settlement_driver_income),
                    value = formattedEarnings,
                    isHighlight = true,
                    valueColor = SuccessColor
                )

                Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

                // Cash / App instruction
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isCashPayment) WarningColor.copy(alpha = 0.12f) else PrimaryColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(AppShape.ShapeM)
                        )
                        .padding(Dimen.PaddingM)
                ) {
                    Text(
                        text = if (isCashPayment) {
                            stringResource(R.string.driver_trip_settlement_cash_collect, formattedTotal)
                        } else {
                            stringResource(R.string.driver_trip_settlement_app_paid)
                        },
                        style = MaterialTheme.typography.s13.bold(),
                        color = if (isCashPayment) WarningColor else PrimaryColor
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissAndFinish,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = OnPrimaryColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                Text(
                    text = stringResource(R.string.driver_trip_btn_finish_and_home),
                    style = MaterialTheme.typography.s16.bold()
                )
            }
        }
    )
}

@Composable
private fun SettlementRow(
    label: String,
    value: String,
    isHighlight: Boolean,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isHighlight) MaterialTheme.typography.s14.semiBold() else MaterialTheme.typography.s13.normal(),
            color = if (isHighlight) TextPrimary else TextSecondary
        )
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.s16.bold() else MaterialTheme.typography.s14.medium(),
            color = valueColor
        )
    }
}
