package com.example.omnigo.core.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface StompClient {
    val connectionState: StateFlow<StompConnectionState>

    fun connect(url: String, connectHeaders: Map<String, String> = emptyMap())

    fun disconnect()

    fun subscribe(destination: String, customHeaders: Map<String, String> = emptyMap()): Flow<StompFrame>

    suspend fun send(
        destination: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap()
    ): Boolean
}
