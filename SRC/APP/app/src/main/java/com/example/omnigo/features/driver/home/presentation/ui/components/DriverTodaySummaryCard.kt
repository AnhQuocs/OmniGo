package com.example.omnigo.features.driver.home.presentation.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold

@Composable
fun DriverTodaySummaryCard(
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
                .padding(Dimen.PaddingM)
        ) {
            Text(
                text = stringResource(id = R.string.driver_today_earnings),
                style = MaterialTheme.typography.s13.normal(),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            Text(
                text = "450.000 đ",
                style = MaterialTheme.typography.s20.bold(),
                color = PrimaryColor
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DriverStatColumn(
                    title = stringResource(id = R.string.driver_completed_trips),
                    value = "8"
                )
                DriverStatColumn(
                    title = stringResource(id = R.string.driver_acceptance_rate),
                    value = "98%"
                )
                DriverStatColumn(
                    title = stringResource(id = R.string.driver_rating),
                    value = "⭐ 4.9"
                )
            }
        }
    }
}

@Composable
fun DriverStatColumn(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.s12.normal(),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(AppSpacing.XXS))
        Text(
            text = value,
            style = MaterialTheme.typography.s16.semiBold(),
            color = TextPrimary
        )
    }
}
