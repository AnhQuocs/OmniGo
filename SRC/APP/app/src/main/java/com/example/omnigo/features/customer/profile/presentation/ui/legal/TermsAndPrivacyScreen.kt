package com.example.omnigo.features.customer.profile.presentation.ui.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.profile.domain.model.LegalContentProvider
import com.example.omnigo.features.customer.profile.domain.model.LegalSection
import com.example.omnigo.features.customer.profile.domain.model.LegalTabType
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndPrivacyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = LegalTabType.entries
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.legal_screen_title),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.order_detail_action_back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Secondary Tab Row synchronized with Pager
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = SurfaceLight,
                contentColor = SecondaryColor,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                        color = SecondaryColor,
                        height = Dimen.PaddingXXS
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = pagerState.currentPage == index
                    val label = when (tab) {
                        LegalTabType.TERMS_OF_SERVICE -> stringResource(id = R.string.legal_tab_terms)
                        LegalTabType.PRIVACY_POLICY -> stringResource(id = R.string.legal_tab_privacy)
                    }

                    Tab(
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = label,
                                style = if (isSelected) {
                                    MaterialTheme.typography.s14.bold()
                                } else {
                                    MaterialTheme.typography.s14.medium()
                                },
                                color = if (isSelected) SecondaryColor else TextSecondary
                            )
                        }
                    )
                }
            }

            // Swipeable HorizontalPager with independent vertical scroll for each page
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val sections = when (tabs[page]) {
                    LegalTabType.TERMS_OF_SERVICE -> LegalContentProvider.termsOfServiceSections
                    LegalTabType.PRIVACY_POLICY -> LegalContentProvider.privacyPolicySections
                }
                LegalTabPageContent(sections = sections)
            }
        }
    }
}

@Composable
private fun LegalTabPageContent(
    sections: List<LegalSection>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingSM),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
    ) {
        // Header Date Info
        Text(
            text = stringResource(id = R.string.legal_last_updated),
            style = MaterialTheme.typography.s12.normal(),
            color = TextSecondary,
            modifier = Modifier.padding(start = Dimen.PaddingXS, top = Dimen.PaddingXS)
        )

        // List of sections
        sections.forEach { section ->
            LegalSectionCard(section = section)
        }

        Spacer(modifier = Modifier.height(AppSpacing.XS))

        // Footer Contact Card
        LegalContactSupportCard()

        Spacer(modifier = Modifier.height(AppSpacing.XL))
    }
}
