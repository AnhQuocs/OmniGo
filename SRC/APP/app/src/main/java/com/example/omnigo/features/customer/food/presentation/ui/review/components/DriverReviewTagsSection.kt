package com.example.omnigo.features.customer.food.presentation.ui.review.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DriverReviewTagsSection(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagToggled: (String) -> Unit,
    isReadOnly: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.food_review_driver_tags_title),
            style = MaterialTheme.typography.s13.medium(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.XXS)
        ) {
            availableTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                FilterChip(
                    selected = isSelected,
                    onClick = { if (!isReadOnly) onTagToggled(tag) },
                    label = {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.s12.normal(),
                            color = if (isSelected) SurfaceLight else TextSecondary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryColor,
                        selectedLabelColor = SurfaceLight
                    ),
                    enabled = !isReadOnly
                )
            }
        }
    }
}
