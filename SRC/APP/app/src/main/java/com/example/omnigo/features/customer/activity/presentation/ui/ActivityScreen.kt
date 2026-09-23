package com.example.omnigo.features.customer.activity.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.core.components.OmniGoMainLayout
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityHeaderSection
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityListSection
import com.example.omnigo.features.customer.activity.presentation.ui.components.ActivityTabsSection

private val TOP_BAR_HEIGHT = 60.dp

@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(id = R.string.activity_tab_ongoing),
        stringResource(id = R.string.activity_tab_history)
    )

    OmniGoMainLayout(
        modifier = modifier,
        headerHeight = TOP_BAR_HEIGHT,
        header = {
            ActivityHeaderSection()
        }
    ) { contentModifier ->
        Column(
            modifier = contentModifier.fillMaxSize()
        ) {
            ActivityTabsSection(
                tabTitles = tabTitles,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Remove this condition to always show mock list or add logic to toggle empty state
                // We'll show the mock list by default to demonstrate the UI
                val isHistory = selectedTabIndex == 1
                ActivityListSection(isHistory = isHistory)

                // If you want to show the empty state, you can toggle a boolean like `hasData`
                // ActivityEmptyStateSection(
                //     icon = if (isHistory) Icons.Filled.History else Icons.Filled.PendingActions,
                //     message = if (isHistory) stringResource(id = R.string.activity_empty_history) else stringResource(id = R.string.activity_empty_ongoing)
                // )
            }
        }
    }
}

