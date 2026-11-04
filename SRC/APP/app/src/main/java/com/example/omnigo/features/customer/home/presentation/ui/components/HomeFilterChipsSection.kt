package com.example.omnigo.features.customer.home.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.features.customer.home.presentation.viewmodel.HomeFilter
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12

private data class FilterChipItem(
    val filter: HomeFilter,
    val titleRes: Int
)

@Composable
fun HomeFilterChipsSection(
    selectedFilter: HomeFilter,
    onFilterSelect: (HomeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        FilterChipItem(HomeFilter.ALL, R.string.home_filter_all),
        FilterChipItem(HomeFilter.NEARBY, R.string.home_filter_nearby),
        FilterChipItem(HomeFilter.PROMOS, R.string.home_filter_promos),
        FilterChipItem(HomeFilter.TOP_RATED, R.string.home_filter_top_rated)
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
        contentPadding = PaddingValues(horizontal = Dimen.PaddingM)
    ) {
        items(filters, key = { it.filter.name }) { item ->
            val isSelected = selectedFilter == item.filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelect(item.filter) },
                label = {
                    Text(
                        text = stringResource(id = item.titleRes),
                        style = if (isSelected) {
                            MaterialTheme.typography.s12.bold()
                        } else {
                            MaterialTheme.typography.s12.normal()
                        }
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryContainer,
                    selectedLabelColor = PrimaryColor,
                    containerColor = SurfaceLight,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) PrimaryColor else CardBorderColor,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(AppShape.ShapeXL2)
            )
        }
    }
}

