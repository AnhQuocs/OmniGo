package com.example.omnigo.features.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import com.example.omnigo.features.home.presentation.ui.components.HomeFilterChipsShimmer
import com.example.omnigo.features.home.presentation.ui.components.HomeHeaderShimmer
import com.example.omnigo.features.home.presentation.ui.components.HomePopularPlacesShimmer
import com.example.omnigo.features.home.presentation.ui.components.HomePromoBannersShimmer
import com.example.omnigo.features.home.presentation.ui.components.HomeQuickWalletShimmer
import com.example.omnigo.features.home.presentation.ui.components.HomeSearchShimmer
import com.example.omnigo.features.home.presentation.ui.components.HomeServicesGridShimmer
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.utils.rememberShimmerBrush

@Composable
fun HomeScreenShimmer(
    modifier: Modifier = Modifier,
    shimmerBrush: Brush = rememberShimmerBrush()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeaderShimmer(shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.height(AppSpacing.XS))

        HomeQuickWalletShimmer(shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.height(AppSpacing.M))

        HomeSearchShimmer(shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.height(AppSpacing.M))

        HomeFilterChipsShimmer(shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.height(AppSpacing.L))

        HomeServicesGridShimmer(shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.height(AppSpacing.LPlus))

        HomePromoBannersShimmer(shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.height(AppSpacing.LPlus))

        HomePopularPlacesShimmer(shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.height(AppSpacing.XXL))
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenShimmerPreview() {
    HomeScreenShimmer()
}
