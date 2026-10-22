package com.example.omnigo.core.websocket

sealed interface StompConnectionState {
    data class Disconnected(val cause: Throwable? = null) : StompConnectionState
    data class Connecting(val endpointUrl: String) : StompConnectionState
    data class Connected(val sessionHeaders: Map<String, String>) : StompConnectionState
    data class Reconnecting(val attempt: Int, val delayMs: Long, val cause: Throwable? = null) : StompConnectionState

    val isConnected: Boolean
        get() = this is Connected
}
