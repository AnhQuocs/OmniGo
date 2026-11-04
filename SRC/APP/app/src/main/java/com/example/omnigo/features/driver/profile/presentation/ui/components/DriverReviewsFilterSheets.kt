package com.example.omnigo.features.driver.profile.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.driver.profile.presentation.state.DriverReviewsUiState
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.RatingGold
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun DriverReviewFilterButtonsRow(
    uiState: DriverReviewsUiState,
    onOpenServiceSheet: () -> Unit,
    onOpenStarSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val serviceLabel = if (uiState.selectedTabIndex == 0) {
        stringResource(id = R.string.driver_reviews_tab_ride)
    } else {
        stringResource(id = R.string.driver_reviews_tab_food)
    }

    val starLabel = if (uiState.selectedStarFilter == 0) {
        stringResource(id = R.string.driver_reviews_filter_all)
    } else {
        stringResource(id = R.string.driver_reviews_star_format, uiState.selectedStarFilter)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.SPlus)
    ) {
        ReviewFilterChipButton(
            title = serviceLabel,
            icon = if (uiState.selectedTabIndex == 0) Icons.Default.DirectionsCar else Icons.Default.Fastfood,
            isActive = true,
            onClick = onOpenServiceSheet,
            modifier = Modifier.weight(1f)
        )

        ReviewFilterChipButton(
            title = starLabel,
            icon = Icons.Default.Star,
            isActive = uiState.selectedStarFilter > 0,
            iconTint = if (uiState.selectedStarFilter > 0) RatingGold else PrimaryColor,
            onClick = onOpenStarSheet,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReviewFilterChipButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: androidx.compose.ui.graphics.Color = PrimaryColor
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppShape.ShapeM),
        color = if (isActive) PrimaryContainer.copy(alpha = 0.5f) else SurfaceLight,
        border = BorderStroke(
            Dimen.BorderHairline,
            if (isActive) PrimaryColor.copy(alpha = 0.6f) else CardBorderColor
        ),
        modifier = modifier.height(Dimen.HeightDefault)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingSM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(Dimen.SizeS)
                )
                Spacer(modifier = Modifier.width(AppSpacing.XS))
                Text(
                    text = title,
                    style = MaterialTheme.typography.s13.semiBold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(Dimen.SizeM)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverReviewServiceFilterBottomSheet(
    selectedTabIndex: Int,
    rideTotalReviews: Int,
    foodTotalReviews: Int,
    onSelectTab: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
        ) {
            Text(
                text = stringResource(id = R.string.driver_reviews_service_sheet_title),
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )

            ServiceOptionRow(
                icon = Icons.Default.DirectionsCar,
                title = stringResource(id = R.string.driver_reviews_tab_ride),
                countText = stringResource(id = R.string.driver_reviews_count_format, rideTotalReviews),
                isSelected = selectedTabIndex == 0,
                onClick = {
                    onSelectTab(0)
                    onDismissRequest()
                }
            )

            HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.5f), thickness = Dimen.BorderHairline)

            ServiceOptionRow(
                icon = Icons.Default.Fastfood,
                title = stringResource(id = R.string.driver_reviews_tab_food),
                countText = stringResource(id = R.string.driver_reviews_count_format, foodTotalReviews),
                isSelected = selectedTabIndex == 1,
                onClick = {
                    onSelectTab(1)
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun ServiceOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    countText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .clickable(onClick = onClick)
            .background(if (isSelected) PrimaryContainer.copy(alpha = 0.4f) else SurfaceLight)
            .padding(Dimen.PaddingM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeL)
                    .clip(CircleShape)
                    .background(if (isSelected) PrimaryColor else PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) SurfaceLight else PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeS)
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.M))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.s14.bold(),
                    color = if (isSelected) PrimaryColor else TextPrimary
                )
                Text(
                    text = countText,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }
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
