package com.example.omnigo.features.home.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
fun HomeServicesGridShimmer(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM)
    ) {
        Box(
            modifier = Modifier
                .width(Dimen.ShimmerTextWidthM)
                .height(Dimen.ShimmerTextHeightL)
                .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        repeat(2) { rowIndex ->
            if (rowIndex > 0) {
                Spacer(modifier = Modifier.height(AppSpacing.MPlus))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(4) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Dimen.SizeXXL)
                                .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeL))
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.XSPlus))
                        Box(
                            modifier = Modifier
                                .width(Dimen.ShimmerTextWidthXS)
                                .height(Dimen.ShimmerTextHeightXS)
                                .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomePromoBannersShimmer(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(horizontal = Dimen.PaddingM)
                .width(Dimen.ShimmerTextWidthL)
                .height(Dimen.ShimmerTextHeightL)
                .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.MediumLarge),
            contentPadding = PaddingValues(horizontal = Dimen.PaddingM)
        ) {
            items(2) {
                Surface(
                    modifier = Modifier
                        .width(Dimen.WidthPromoBanner)
                        .height(Dimen.HeightPromoBanner),
                    shape = RoundedCornerShape(AppShape.ShapeL),
                    color = SurfaceLight,
                    border = BorderStroke(1.dp, CardBorderColor),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimen.PaddingM),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(Dimen.ShimmerTextWidthS)
                                    .height(Dimen.ShimmerTextHeightM)
                                    .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                            )
                            Box(
                                modifier = Modifier
                                    .size(Dimen.SizeL)
                                    .shimmerBackground(shimmerBrush, CircleShape)
                            )
                        }

                        Column {
                            Box(
                                modifier = Modifier
                                    .width(Dimen.ShimmerTextWidthL)
                                    .height(Dimen.ShimmerTextHeightL)
                                    .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.XXS))
                            Box(
                                modifier = Modifier
                                    .width(Dimen.ShimmerTextWidthXXL)
                                    .height(Dimen.ShimmerTextHeightS)
                                    .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(Dimen.ShimmerTextWidthM)
                                .height(Dimen.SizeM)
                                .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXL2))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomePopularPlacesShimmer(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(Dimen.ShimmerTextWidthL)
                    .height(Dimen.ShimmerTextHeightL)
                    .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
            )

            Box(
                modifier = Modifier
                    .width(Dimen.ShimmerTextWidthS)
                    .height(Dimen.ShimmerTextHeightM)
                    .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        repeat(3) { index ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(AppSpacing.SPlus))
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppShape.ShapeM),
                color = SurfaceLight,
                border = BorderStroke(1.dp, CardBorderColor),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                .size(Dimen.SizeXXL)
                                .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeS))
                        )

                        Spacer(modifier = Modifier.width(AppSpacing.M))

                        Column {
                            Box(
                                modifier = Modifier
                                    .width(Dimen.ShimmerTextWidthXL)
                                    .height(Dimen.ShimmerTextHeightL)
                                    .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.XS))
                            Box(
                                modifier = Modifier
                                    .width(Dimen.ShimmerTextWidthXXL)
                                    .height(Dimen.ShimmerTextHeightS)
                                    .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(Dimen.ShimmerTextWidthXXS)
                            .height(Dimen.ShimmerTextHeightL)
                            .shimmerBackground(shimmerBrush, RoundedCornerShape(AppShape.ShapeXXS))
                    )
                }
            }
        }
    }
}
