package com.example.omnigo.features.driver.trips.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.trips.domain.model.DriverTripFilter
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.semiBold

private data class FilterChipItem(
    val filter: DriverTripFilter,
    val labelRes: Int,
    val icon: ImageVector
)

@Composable
fun DriverTripFilterChips(
    selectedFilter: DriverTripFilter,
    onFilterSelected: (DriverTripFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val chips = listOf(
        FilterChipItem(
            filter = DriverTripFilter.ALL,
            labelRes = R.string.driver_trips_filter_all,
            icon = Icons.Default.FilterList
        ),
        FilterChipItem(
            filter = DriverTripFilter.RIDE,
            labelRes = R.string.driver_trips_filter_ride,
            icon = Icons.Default.DirectionsCar
        ),
        FilterChipItem(
            filter = DriverTripFilter.FOOD,
            labelRes = R.string.driver_trips_filter_food,
            icon = Icons.Default.ShoppingBag
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chips.forEach { item ->
            val isSelected = selectedFilter == item.filter
            val bg = if (isSelected) PrimaryContainer.copy(alpha = 0.7f) else SurfaceLight
            val borderClr = if (isSelected) PrimaryColor else CardBorderColor
            val contentClr = if (isSelected) PrimaryColor else TextSecondary

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.ShapeXL))
                    .background(bg)
                    .border(Dimen.BorderHairline, borderClr, RoundedCornerShape(AppShape.ShapeXL))
                    .clickable { onFilterSelected(item.filter) }
                    .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = contentClr,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XSPlus))
                    Text(
                        text = stringResource(id = item.labelRes),
                        style = MaterialTheme.typography.s13.semiBold(),
                        color = if (isSelected) PrimaryColor else TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
