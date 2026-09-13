package com.example.omnigo.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.omnigo.R
import com.example.omnigo.core.network.ConnectivityObserver
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s13
import kotlinx.coroutines.delay

@Composable
fun ConnectivityBanner(
    status: ConnectivityObserver.Status,
    modifier: Modifier = Modifier
) {
    var showRestored by remember { mutableStateOf(false) }

    LaunchedEffect(status) {
        if (status == ConnectivityObserver.Status.Available) {
            showRestored = true
            delay(3000L) // Hiển thị thông báo khôi phục trong 3s
            showRestored = false
        } else {
            showRestored = false
        }
    }

    val isOffline = status != ConnectivityObserver.Status.Available && !showRestored
    val isRestored = showRestored

    AnimatedVisibility(
        visible = isOffline || isRestored,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier.fillMaxWidth()
    ) {
        val backgroundColor = if (isRestored) SuccessColor else ErrorColor
        val message = if (isRestored) {
            stringResource(id = R.string.connectivity_restored)
        } else {
            stringResource(id = R.string.connectivity_no_internet)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .statusBarsPadding()
                .padding(vertical = Dimen.PaddingXS, horizontal = Dimen.PaddingM),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = TextWhite,
                style = MaterialTheme.typography.s13.medium(),
                textAlign = TextAlign.Center
            )
        }
    }
}
