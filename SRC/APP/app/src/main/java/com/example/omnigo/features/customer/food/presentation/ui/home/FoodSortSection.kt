package com.example.omnigo.features.customer.food.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.util.RestaurantSort
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12

@Composable
fun FoodSortSection(
    selectedSort: RestaurantSort,
    onSortSelected: (RestaurantSort) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortOptions = listOf(
        RestaurantSort.RECOMMENDED to R.string.food_sort_recommended,
        RestaurantSort.TOP_RATED to R.string.food_sort_top_rated,
        RestaurantSort.NEAREST to R.string.food_sort_nearest
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeXL))
            .background(SurfaceLight)
            .padding(Dimen.PaddingXXS)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            sortOptions.forEach { (sort, labelRes) ->
                val isSelected = selectedSort == sort

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(AppShape.ShapeXL))
                        .background(if (isSelected) PrimaryContainer else SurfaceLight)
                        .clickable { onSortSelected(sort) }
                        .padding(vertical = Dimen.PaddingS, horizontal = Dimen.PaddingXS),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = labelRes),
                        style = if (isSelected) MaterialTheme.typography.s12.bold() else MaterialTheme.typography.s12.medium(),
                        color = if (isSelected) PrimaryColor else TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
