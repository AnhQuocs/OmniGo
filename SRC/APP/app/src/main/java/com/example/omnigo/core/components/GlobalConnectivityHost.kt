package com.example.omnigo.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.core.network.ConnectivityViewModel

@Composable
fun GlobalConnectivityHost(
    content: @Composable () -> Unit
) {
    val viewModel: ConnectivityViewModel = hiltViewModel()
    val status by viewModel.connectivityStatus.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        ConnectivityBanner(status = status)
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}
