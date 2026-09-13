package com.example.omnigo.features.onboarding.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.omnigo.R
import com.example.omnigo.core.components.AppButton
import com.example.omnigo.features.onboarding.presentation.ui.components.OnboardingPagerItem
import com.example.omnigo.features.onboarding.presentation.ui.components.PagerIndicator
import com.example.omnigo.features.onboarding.presentation.viewmodel.OnboardingViewModel
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.OmniGoTheme
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s15
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val pages = viewModel.pages
    val pagerState = rememberPagerState(pageCount = { pages.size })

    OmniGoTheme(darkTheme = false) {
        Scaffold(
            containerColor = BackgroundLight,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar with Skip Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.SizeXLPlus),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage < pages.size - 1) {
                        TextButton(
                            onClick = {
                                viewModel.completeOnboarding(onSuccess = onNavigateToAuth)
                            }
                        ) {
                            Text(
                                text = stringResource(id = R.string.onboarding_skip),
                                style = MaterialTheme.typography.s15.medium(),
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Center Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { pageIndex ->
                    OnboardingPagerItem(page = pages[pageIndex])
                }

                // Bottom Indicator and Action Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimen.PaddingM),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PagerIndicator(
                        pageCount = pages.size,
                        currentPage = pagerState.currentPage
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.XL))

                    val isLastPage = pagerState.currentPage == pages.size - 1

                    AppButton(
                        text = stringResource(
                            id = if (isLastPage) R.string.onboarding_get_started else R.string.onboarding_next
                        ),
                        onClick = {
                            if (isLastPage) {
                                viewModel.completeOnboarding(onSuccess = onNavigateToAuth)
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
