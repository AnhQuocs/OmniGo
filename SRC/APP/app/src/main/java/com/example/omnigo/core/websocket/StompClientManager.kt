package com.example.omnigo.core.websocket

import com.example.omnigo.BuildConfig
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.di.WebSocketOkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StompClientManager @Inject constructor(
    @param:WebSocketOkHttp private val okHttpClient: OkHttpClient,
    private val sessionManager: SessionManager,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    val bookingClient: StompClient by lazy {
        DefaultStompClient(
            okHttpClient = okHttpClient,
            scope = scope,
            headersProvider = { createAuthHeaders() }
        )
    }

    val locationClient: StompClient by lazy {
        DefaultStompClient(
            okHttpClient = okHttpClient,
            scope = scope,
            headersProvider = { createAuthHeaders() }
        )
    }

    init {
        observeSessionChanges()
    }

    private fun observeSessionChanges() {
        scope.launch {
            sessionManager.isLoggedInFlow.collectLatest { isLoggedIn ->
                if (isLoggedIn) {
                    connectAll()
                } else {
                    disconnectAll()
                }
            }
        }
    }

    suspend fun connectBooking(baseUrl: String = BuildConfig.BASE_URL) {
        val url = buildWebSocketUrl(baseUrl, BOOKING_ENDPOINT)
        val headers = createAuthHeaders()
        bookingClient.connect(url, headers)
    }

    suspend fun connectLocation(baseUrl: String = BuildConfig.BASE_URL) {
        val url = buildWebSocketUrl(baseUrl, LOCATION_ENDPOINT)
        val headers = createAuthHeaders()
        locationClient.connect(url, headers)
    }

    suspend fun connectAll(baseUrl: String = BuildConfig.BASE_URL) {
        connectBooking(baseUrl)
        connectLocation(baseUrl)
    }

    suspend fun ensureBookingConnected(baseUrl: String = BuildConfig.BASE_URL) {
        val userId = sessionManager.getUserId()
        if (userId != null && userId > 0 && bookingClient.connectionState.value is StompConnectionState.Disconnected) {
            connectBooking(baseUrl)
        }
    }

    suspend fun ensureLocationConnected(baseUrl: String = BuildConfig.BASE_URL) {
        val userId = sessionManager.getUserId()
        if (userId != null && userId > 0 && locationClient.connectionState.value is StompConnectionState.Disconnected) {
            connectLocation(baseUrl)
        }
    }

    fun disconnectAll() {
        bookingClient.disconnect()
        locationClient.disconnect()
    }

    private suspend fun createAuthHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        val userId = sessionManager.getUserId()
        if (userId != null && userId > 0) {
            headers["userId"] = userId.toString()
        }
        val token = sessionManager.getAccessToken()
        if (!token.isNullOrBlank()) {
            headers["Authorization"] = "Bearer $token"
        }
        return headers
    }

    companion object {
        const val BOOKING_ENDPOINT = "ws-booking"
        const val LOCATION_ENDPOINT = "ws-location"

        fun buildWebSocketUrl(httpBaseUrl: String, endpoint: String): String {
            val trimmed = httpBaseUrl.trimEnd('/')
            val wsBase = when {
                trimmed.startsWith("https://", ignoreCase = true) -> "wss://" + trimmed.substring(8)
                trimmed.startsWith("http://", ignoreCase = true) -> "ws://" + trimmed.substring(7)
                trimmed.startsWith("wss://", ignoreCase = true) || trimmed.startsWith("ws://", ignoreCase = true) -> trimmed
                else -> "ws://$trimmed"
            }
            val cleanEndpoint = endpoint.trimStart('/')
            // Standard SockJS pure-websocket transport endpoint suffix
            return "$wsBase/$cleanEndpoint/websocket"
        }
    }
}
