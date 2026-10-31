package com.example.omnigo.features.driver.trips.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DriverTimelineSectionHeader(
    title: String,
    totalTrips: Int,
    totalEarnings: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.PaddingXSPlus),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeS)
            )
            Spacer(modifier = Modifier.width(AppSpacing.XSPlus))
            Text(
                text = title,
                style = MaterialTheme.typography.s14.bold(),
                color = TextPrimary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.ShapeS))
                    .background(PrimaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
            ) {
                Text(
                    text = stringResource(id = R.string.driver_trips_timeline_trips_count, totalTrips),
                    style = MaterialTheme.typography.s12.semiBold(),
                    color = PrimaryColor
                )
            }

            if (totalEarnings > 0) {
                Text(
                    text = String.format(Locale.getDefault(), "%,.0f ₫", totalEarnings),
                    style = MaterialTheme.typography.s13.semiBold(),
                    color = PrimaryColor
                )
            }
        }
    }
}
