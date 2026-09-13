package com.example.omnigo.features.home.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.utils.shimmerBackground

@Composable
fun HomeHeaderShimmer(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(AppShape.ShapeXL2),
            color = SurfaceLight,
            border = BorderStroke(1.dp, CardBorderColor),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = Dimen.PaddingSM,
                    vertical = Dimen.PaddingXSPlus
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .shimmerBackground(shimmerBrush, CircleShape)
                )

                Spacer(modifier = Modifier.width(AppSpacing.XSPlus))

                Column {
                    Box(
                        modifier = Modifier
                            .width(Dimen.ShimmerTextWidthXS)
                            .height(Dimen.ShimmerTextHeightXS)
                            .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Box(
                        modifier = Modifier
                            .width(Dimen.ShimmerTextWidthML)
                            .height(Dimen.ShimmerTextHeightM)
                            .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.XS))

                Box(
                    modifier = Modifier
                        .size(Dimen.SizeS)
                        .shimmerBackground(shimmerBrush, CircleShape)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(Dimen.SizeXLPlus)
                .shimmerBackground(shimmerBrush, CircleShape)
        )
    }
}

@Composable
fun HomeQuickWalletShimmer(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM),
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = SurfaceLight,
        border = BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeWalletIcon)
                        .shimmerBackground(shimmerBrush, CircleShape)
                )

                Spacer(modifier = Modifier.width(AppSpacing.SPlus))

                Column {
                    Box(
                        modifier = Modifier
                            .width(Dimen.ShimmerTextWidthM)
                            .height(Dimen.ShimmerTextHeightS)
                            .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Box(
                        modifier = Modifier
                            .width(Dimen.ShimmerTextWidthML)
                            .height(Dimen.ShimmerTextHeightL)
                            .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)) {
                Box(
                    modifier = Modifier
                        .width(Dimen.ShimmerTextWidthSM)
                        .height(Dimen.SizeL)
                        .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXL2))
                )
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeL)
                        .shimmerBackground(shimmerBrush, CircleShape)
                )
            }
        }
    }
}

@Composable
fun HomeSearchShimmer(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM),
        shape = RoundedCornerShape(AppShape.ShapeXL2),
        color = SurfaceLight,
        border = BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeM)
                        .shimmerBackground(shimmerBrush, CircleShape)
                )

                Spacer(modifier = Modifier.width(AppSpacing.SPlus))

                Box(
                    modifier = Modifier
                        .width(Dimen.ShimmerTextWidthXL)
                        .height(Dimen.ShimmerTextHeightM)
                        .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                )
            }

            Box(
                modifier = Modifier
                    .size(Dimen.SizeM)
                    .shimmerBackground(shimmerBrush, CircleShape)
            )
        }
    }
}

@Composable
fun HomeFilterChipsShimmer(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    val chipWidths = listOf(
        Dimen.ShimmerTextWidthS,
        Dimen.ShimmerTextWidthSM,
        Dimen.ShimmerTextWidthM,
        Dimen.ShimmerTextWidthS
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
        contentPadding = PaddingValues(horizontal = Dimen.PaddingM)
    ) {
        items(chipWidths.size) { index ->
            Box(
                modifier = Modifier
                    .width(chipWidths[index])
                    .height(Dimen.SizeL)
                    .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXL2))
            )
        }
    }
}
