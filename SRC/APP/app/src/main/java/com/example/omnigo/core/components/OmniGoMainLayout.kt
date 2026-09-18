package com.example.omnigo.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.omnigo.features.main.presentation.ui.components.LocalBottomNavVisibility
import com.example.omnigo.features.main.presentation.ui.components.animateToolbarOffset
import com.example.omnigo.features.main.presentation.ui.components.rememberCollapsingBarsState
import com.example.omnigo.ui.theme.BackgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniGoMainLayout(
    modifier: Modifier = Modifier,
    headerHeight: Dp = 60.dp,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    header: @Composable () -> Unit,
    content: @Composable ColumnScope.(Modifier) -> Unit
) {
    val density = LocalDensity.current
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density).toFloat()
    val contentHeaderHeightPx = with(density) { headerHeight.toPx() }
    val fullHeaderHeightPx = statusBarHeightPx + contentHeaderHeightPx

    val scrollThresholdPx = headerHeight.value + 20f

    val bottomNavController = LocalBottomNavVisibility.current
    val barsState = rememberCollapsingBarsState(scrollThreshold = scrollThresholdPx)

    val headerOffset = animateToolbarOffset(barsState.barsVisible, fullHeaderHeightPx)

    LaunchedEffect(barsState.barsVisible) {
        bottomNavController.visible = barsState.barsVisible
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        val offsetModifier = Modifier.offset {
            val visibleHeaderHeight = (fullHeaderHeightPx + headerOffset).coerceAtLeast(0f)
            IntOffset(0, visibleHeaderHeight.toInt())
        }

        val nestedScrollModifier = Modifier.nestedScroll(barsState.nestedScrollConnection)

        if (onRefresh != null) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .then(nestedScrollModifier)
                    .then(offsetModifier)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    content(Modifier)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(nestedScrollModifier)
                    .then(offsetModifier)
            ) {
                content(Modifier)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .offset { IntOffset(0, headerOffset.toInt()) }
        ) {
            header()
        }
    }
}