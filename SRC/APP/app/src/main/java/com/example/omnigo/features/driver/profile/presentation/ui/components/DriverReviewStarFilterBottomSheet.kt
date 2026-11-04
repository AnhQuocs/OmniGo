package com.example.omnigo.features.driver.profile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.profile.presentation.state.DriverReviewsUiState
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.RatingGold
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverReviewStarFilterBottomSheet(
    selectedStar: Int,
    uiState: DriverReviewsUiState,
    onSelectStar: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val starOptions = listOf(0, 5, 4, 3, 2, 1)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXXL, topEnd = AppShape.ShapeXXL)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingL)
                .padding(bottom = Dimen.PaddingL)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
        ) {
            Text(
                text = stringResource(id = R.string.driver_reviews_star_sheet_title),
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.XXS))

            starOptions.forEachIndexed { index, star ->
                val isSelected = selectedStar == star
                val count = if (star == 0) uiState.currentTotalReviews else uiState.starCountForCurrentTab(star)

                StarOptionRow(
                    star = star,
                    count = count,
                    isSelected = isSelected,
                    onClick = {
                        onSelectStar(star)
                        onDismissRequest()
                    }
                )

                if (index < starOptions.size - 1) {
                    HorizontalDivider(
                        color = OutlineVariantLight.copy(alpha = 0.4f),
                        thickness = Dimen.BorderHairline
                    )
                }
            }
        }
    }
}

@Composable
private fun StarOptionRow(
    star: Int,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .clickable(onClick = onClick)
            .background(if (isSelected) PrimaryContainer.copy(alpha = 0.35f) else SurfaceLight)
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (star == 0) {
                Text(
                    text = stringResource(id = R.string.driver_reviews_filter_all),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = if (isSelected) PrimaryColor else TextPrimary
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(star) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = RatingGold,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = stringResource(id = R.string.driver_reviews_star_format, star),
                        style = MaterialTheme.typography.s14.medium(),
                        color = if (isSelected) PrimaryColor else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.S))

            Text(
                text = "(${stringResource(id = R.string.driver_reviews_count_format, count)})",
                style = MaterialTheme.typography.s12.normal(),
                color = TextSecondary
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeSM)
            )
        }
    }
}

