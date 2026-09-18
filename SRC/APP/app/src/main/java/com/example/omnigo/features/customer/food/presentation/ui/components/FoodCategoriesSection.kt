package com.example.omnigo.features.customer.food.presentation.ui.components

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
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12

data class FoodCategoryItem(
    val id: String,
    val labelRes: Int
)

@Composable
fun FoodCategoriesSection(
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        FoodCategoryItem("all", R.string.food_filter_all),
        FoodCategoryItem("bún", R.string.food_filter_bun),
        FoodCategoryItem("cơm", R.string.food_filter_com),
        FoodCategoryItem("pizza", R.string.food_filter_pizza),
        FoodCategoryItem("drink", R.string.food_filter_drink)
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
        contentPadding = PaddingValues(horizontal = Dimen.PaddingM)
    ) {
        items(categories) { category ->
            val isSelected = if (category.id == "all") {
                selectedCategoryId == null || selectedCategoryId == "all"
            } else {
                selectedCategoryId == category.id
            }

            FilterChip(
                selected = isSelected,
                onClick = {
                    if (category.id == "all") {
                        onCategorySelected(null)
                    } else {
                        onCategorySelected(category.id)
                    }
                },
                label = {
                    Text(
                        text = stringResource(id = category.labelRes),
                        style = MaterialTheme.typography.s12.medium()
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
