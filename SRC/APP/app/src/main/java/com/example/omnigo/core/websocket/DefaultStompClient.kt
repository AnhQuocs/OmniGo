package com.example.omnigo.core.websocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class DefaultStompClient(
    private val okHttpClient: OkHttpClient,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val headersProvider: (suspend () -> Map<String, String>)? = null,
    private val clientHeartbeatMs: Long = 10000L
) : StompClient {

    private val _connectionState = MutableStateFlow<StompConnectionState>(StompConnectionState.Disconnected())
    override val connectionState: StateFlow<StompConnectionState> = _connectionState.asStateFlow()

    private var activeWebSocket: WebSocket? = null
    private var targetUrl: String? = null
    private var targetHeaders: Map<String, String> = emptyMap()

    private val shouldStayConnected = AtomicBoolean(false)
    private val subCounter = AtomicInteger(0)
    private val subscriptions = ConcurrentHashMap<String, ActiveSubscription>()

    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null
    private var reconnectAttempt = 0

    private data class ActiveSubscription(
        val subId: String,
        val destination: String,
        val headers: Map<String, String>,
        val channel: ProducerScope<StompFrame>
    )

    override fun connect(url: String, connectHeaders: Map<String, String>) {
        shouldStayConnected.set(true)
        targetUrl = url
        targetHeaders = connectHeaders
        reconnectJob?.cancel()
        reconnectAttempt = 0

        // Prevent duplicate or orphaned connections
        activeWebSocket?.let { oldWs ->
            try {
                oldWs.close(1000, "Resetting connection")
            } catch (_: Exception) {
            }
            activeWebSocket = null
        }

        initiateWebSocketConnection(url)
    }

    override fun disconnect() {
        shouldStayConnected.set(false)
        reconnectJob?.cancel()
        reconnectJob = null
        heartbeatJob?.cancel()
        heartbeatJob = null
        reconnectAttempt = 0
        try {
            activeWebSocket?.let { ws ->
                val disconnectFrame = StompFrame(StompCommand.DISCONNECT)
                ws.send(disconnectFrame.toRawString())
                ws.close(1000, "Normal closure")
            }
        } catch (_: Exception) {
        } finally {
            activeWebSocket = null
            _connectionState.value = StompConnectionState.Disconnected()
        }
    }

    override fun subscribe(destination: String, customHeaders: Map<String, String>): Flow<StompFrame> = callbackFlow {
        val subId = "sub-${subCounter.incrementAndGet()}"
        val sub = ActiveSubscription(
            subId = subId,
            destination = destination,
            headers = customHeaders,
            channel = this
        )
        subscriptions[subId] = sub

        if (_connectionState.value is StompConnectionState.Connected) {
            sendSubscribeFrame(sub)
        }

        awaitClose {
            subscriptions.remove(subId)
            if (_connectionState.value is StompConnectionState.Connected) {
                sendUnsubscribeFrame(subId)
            }
        }
    }

    override suspend fun send(destination: String, body: String?, headers: Map<String, String>): Boolean {
        val ws = activeWebSocket ?: return false
        val allHeaders = mutableMapOf("destination" to destination)
        if (!body.isNullOrEmpty()) {
            allHeaders["content-length"] = body.toByteArray(Charsets.UTF_8).size.toString()
        }
        allHeaders.putAll(headers)
        val frame = StompFrame(StompCommand.SEND, allHeaders, body)
        return ws.send(frame.toRawString())
    }

    private fun initiateWebSocketConnection(url: String) {
        _connectionState.value = StompConnectionState.Connecting(url)
        val request = Request.Builder().url(url).build()

        activeWebSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                scope.launch {
                    val connectHeaders = mutableMapOf(
                        "accept-version" to "1.1,1.2",
                        "heart-beat" to "$clientHeartbeatMs,$clientHeartbeatMs"
                    )
                    // Obtain dynamic fresh headers on every handshake to handle token refresh
                    val resolvedHeaders = try {
                        headersProvider?.invoke() ?: targetHeaders
                    } catch (_: Exception) {
                        targetHeaders
                    }
                    connectHeaders.putAll(resolvedHeaders)
                    val connectFrame = StompFrame(StompCommand.CONNECT, connectHeaders)
                    webSocket.send(connectFrame.toRawString())
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingMessage(webSocket, text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                cleanupConnection()
                _connectionState.value = StompConnectionState.Disconnected()
                triggerReconnectIfNeeded()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                cleanupConnection()
                _connectionState.value = StompConnectionState.Disconnected(t)
                triggerReconnectIfNeeded(t)
            }
        })
    }

    private fun cleanupConnection() {
        activeWebSocket = null
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun handleIncomingMessage(webSocket: WebSocket, rawText: String) {
        val frames = StompFrame.parse(rawText)
        for (frame in frames) {
            when (frame.command) {
                StompCommand.CONNECTED -> {
                    reconnectAttempt = 0
                    _connectionState.value = StompConnectionState.Connected(frame.headers)
                    setupHeartbeat(frame.headers)
                    resubscribeAll()
                }
                StompCommand.MESSAGE -> {
                    val subId = frame.headers["subscription"]
                    val target = subId?.let { subscriptions[it] }
                    if (target != null) {
                        target.channel.trySend(frame)
                    } else {
                        val destination = frame.headers["destination"]
                        subscriptions.values
                            .filter { it.destination == destination }
                            .forEach { it.channel.trySend(frame) }
                    }
                }
                StompCommand.ERROR -> {
                    // Broker error frame received
                }
                else -> {
                    // Other frames (RECEIPT, etc.)
                }
            }
        }
    }

    private fun setupHeartbeat(headers: Map<String, String>) {
        val serverHeartbeat = headers["heart-beat"] ?: return
        val parts = serverHeartbeat.split(",")
        if (parts.size >= 2) {
            val serverSend = parts[0].trim().toLongOrNull() ?: 0L
            val serverReceive = parts[1].trim().toLongOrNull() ?: 0L

            // Client send interval = max(clientHeartbeatMs, serverReceive) if both > 0
            if (clientHeartbeatMs > 0 && serverReceive > 0) {
                val negotiatedIntervalMs = maxOf(clientHeartbeatMs, serverReceive)
                startHeartbeat(negotiatedIntervalMs)
            }
        }
    }

    private fun startHeartbeat(intervalMs: Long) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(intervalMs)
                activeWebSocket?.send("\n")
            }
        }
    }

    private fun resubscribeAll() {
        subscriptions.values.forEach { sub ->
            sendSubscribeFrame(sub)
        }
    }

    private fun sendSubscribeFrame(sub: ActiveSubscription) {
        val ws = activeWebSocket ?: return
        val headers = mutableMapOf(
            "id" to sub.subId,
            "destination" to sub.destination,
            "ack" to "auto"
        )
        headers.putAll(sub.headers)
        val frame = StompFrame(StompCommand.SUBSCRIBE, headers)
        ws.send(frame.toRawString())
    }

    private fun sendUnsubscribeFrame(subId: String) {
        val ws = activeWebSocket ?: return
        val headers = mapOf("id" to subId)
        val frame = StompFrame(StompCommand.UNSUBSCRIBE, headers)
        ws.send(frame.toRawString())
    }

    private fun triggerReconnectIfNeeded(cause: Throwable? = null) {
        if (!shouldStayConnected.get()) return
        val url = targetUrl ?: return

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val delayMs = (1000L * (1 shl reconnectAttempt.coerceAtMost(4))).coerceAtMost(16000L)
            reconnectAttempt++
            _connectionState.value = StompConnectionState.Reconnecting(reconnectAttempt, delayMs, cause)
            delay(delayMs)
            if (shouldStayConnected.get()) {
                initiateWebSocketConnection(url)
            }
        }
    }
}
