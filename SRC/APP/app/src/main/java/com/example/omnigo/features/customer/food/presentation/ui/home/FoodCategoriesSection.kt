package com.example.omnigo.features.customer.food.presentation.ui.home

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
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s13

@Composable
fun FoodCategoriesSection(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
        contentPadding = PaddingValues(horizontal = Dimen.PaddingM)
    ) {
        // "Tất cả" chip
        item {
            val isAllSelected = selectedCategory.isNullOrBlank()
            FilterChip(
                selected = isAllSelected,
                onClick = { onCategorySelected(null) },
                label = {
                    Text(
                        text = stringResource(id = R.string.food_filter_all),
                        style = if (isAllSelected) MaterialTheme.typography.s13.bold() else MaterialTheme.typography.s13.medium()
                    )
                },
                shape = RoundedCornerShape(AppShape.ShapeXL2),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = SurfaceLight,
                    labelColor = TextPrimary,
                    selectedContainerColor = PrimaryContainer,
                    selectedLabelColor = PrimaryColor
                )
            )
        }

        items(categories, key = { it }) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        style = if (isSelected) MaterialTheme.typography.s13.bold() else MaterialTheme.typography.s13.medium()
                    )
                },
                shape = RoundedCornerShape(AppShape.ShapeXL2),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = SurfaceLight,
                    labelColor = TextPrimary,
                    selectedContainerColor = PrimaryContainer,
                    selectedLabelColor = PrimaryColor
                )
            )
        }
    }
}
