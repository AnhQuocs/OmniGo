package com.example.omnigo.core.websocket

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultStompClientTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val mockOkHttpClient = mockk<OkHttpClient>()
    private val mockWebSocket = mockk<WebSocket>(relaxed = true)
    private val listenerSlot = slot<WebSocketListener>()
    private val requestSlot = slot<Request>()

    @Before
    fun setup() {
        every {
            mockOkHttpClient.newWebSocket(capture(requestSlot), capture(listenerSlot))
        } returns mockWebSocket
    }

    @Test
    fun `initial state is Disconnected`() = testScope.runTest {
        val client = DefaultStompClient(
            okHttpClient = mockOkHttpClient,
            scope = this
        )

        assertTrue(client.connectionState.value is StompConnectionState.Disconnected)
    }

    @Test
    fun `connect establishes web socket and sends CONNECT frame on open`() = testScope.runTest {
        val client = DefaultStompClient(
            okHttpClient = mockOkHttpClient,
            scope = this
        )

        client.connect(
            url = "ws://localhost:8080/ws-booking/websocket",
            connectHeaders = mapOf("Authorization" to "Bearer abc", "userId" to "10")
        )
        advanceUntilIdle()

        assertTrue(listenerSlot.isCaptured)
        val listener = listenerSlot.captured
        listener.onOpen(mockWebSocket, mockk(relaxed = true))
        advanceUntilIdle()

        val sentFrames = mutableListOf<String>()
        verify { mockWebSocket.send(capture(sentFrames)) }
        assertTrue(sentFrames.any { it.startsWith("CONNECT\n") && it.contains("userId:10") })
    }

    @Test
    fun `receiving CONNECTED frame sets connection state to Connected`() = testScope.runTest {
        val client = DefaultStompClient(
            okHttpClient = mockOkHttpClient,
            scope = this
        )

        client.connect(
            url = "ws://localhost:8080/ws-booking/websocket",
            connectHeaders = emptyMap()
        )
        advanceUntilIdle()

        val listener = listenerSlot.captured
        listener.onOpen(mockWebSocket, mockk(relaxed = true))
        advanceUntilIdle()

        listener.onMessage(mockWebSocket, "CONNECTED\nversion:1.2\n\n\u0000")
        advanceUntilIdle()

        assertTrue(client.connectionState.value is StompConnectionState.Connected)
    }

    @Test
    fun `subscribed flow receives MESSAGE frame matching subscription id`() = testScope.runTest {
        val client = DefaultStompClient(
            okHttpClient = mockOkHttpClient,
            scope = this
        )

        client.connect(
            url = "ws://localhost:8080/ws-booking/websocket",
            connectHeaders = emptyMap()
        )
        advanceUntilIdle()

        val listener = listenerSlot.captured
        listener.onOpen(mockWebSocket, mockk(relaxed = true))
        listener.onMessage(mockWebSocket, "CONNECTED\nversion:1.2\n\n\u0000")
        advanceUntilIdle()

        val destination = "/user/queue/food-order/status"
        val subscriptionFlow = client.subscribe(destination)

        subscriptionFlow.test {
            advanceUntilIdle()

            // Server emits a message for sub-1
            val rawMsg = "MESSAGE\nsubscription:sub-1\ndestination:$destination\n\n{\"status\":\"CONFIRMED\"}\u0000"
            listener.onMessage(mockWebSocket, rawMsg)

            val item = awaitItem()
            assertEquals(StompCommand.MESSAGE, item.command)
            assertEquals("{\"status\":\"CONFIRMED\"}", item.body)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `disconnect moves state to Disconnected and closes web socket`() = testScope.runTest {
        val client = DefaultStompClient(
            okHttpClient = mockOkHttpClient,
            scope = this
        )

        client.connect(
            url = "ws://localhost:8080/ws-booking/websocket",
            connectHeaders = emptyMap()
        )
        advanceUntilIdle()

        val listener = listenerSlot.captured
        listener.onOpen(mockWebSocket, mockk(relaxed = true))
        listener.onMessage(mockWebSocket, "CONNECTED\nversion:1.2\n\n\u0000")
        advanceUntilIdle()

        client.disconnect()
        advanceUntilIdle()

        assertTrue(client.connectionState.value is StompConnectionState.Disconnected)
        verify { mockWebSocket.close(1000, any()) }
    }

    @Test
    fun `connect resolves headers from headersProvider dynamically on open`() = testScope.runTest {
        var currentToken = "token-1"
        val client = DefaultStompClient(
            okHttpClient = mockOkHttpClient,
            scope = this,
            headersProvider = { mapOf("Authorization" to "Bearer $currentToken") }
        )

        client.connect(url = "ws://localhost:8080/ws-booking/websocket")
        advanceUntilIdle()

        val listener = listenerSlot.captured
        listener.onOpen(mockWebSocket, mockk(relaxed = true))
        advanceUntilIdle()

        val sentFrames = mutableListOf<String>()
        verify { mockWebSocket.send(capture(sentFrames)) }
        assertTrue(sentFrames.any { it.contains("Authorization:Bearer token-1") })

        // Simulate reconnect with token update
        currentToken = "token-2"
        val sentFrames2 = mutableListOf<String>()
        listener.onOpen(mockWebSocket, mockk(relaxed = true))
        advanceUntilIdle()

        verify { mockWebSocket.send(capture(sentFrames2)) }
        assertTrue(sentFrames2.any { it.contains("Authorization:Bearer token-2") })
    }

    @Test
    fun `calling connect multiple times closes previous websocket before opening new one`() = testScope.runTest {
        val client = DefaultStompClient(
            okHttpClient = mockOkHttpClient,
            scope = this
        )

        client.connect(url = "ws://localhost:8080/ws-booking/websocket")
        advanceUntilIdle()

        val listener = listenerSlot.captured
        listener.onOpen(mockWebSocket, mockk(relaxed = true))
        advanceUntilIdle()

        // Calling connect again should close the old webSocket
        client.connect(url = "ws://localhost:8080/ws-booking/websocket")
        advanceUntilIdle()

        verify { mockWebSocket.close(1000, any()) }
    }

    @Test
    fun `heartbeat ticker sends newline byte periodically when negotiated`() = testScope.runTest {
        val client = DefaultStompClient(
            okHttpClient = mockOkHttpClient,
            scope = this
        )

        client.connect(url = "ws://localhost:8080/ws-booking/websocket")
        testScheduler.runCurrent()

        val listener = listenerSlot.captured
        listener.onOpen(mockWebSocket, mockk(relaxed = true))
        testScheduler.runCurrent()

        // Server negotiates heartbeat cx=0, sy=10000 -> client should send at max(10000, 10000) = 10000ms
        listener.onMessage(mockWebSocket, "CONNECTED\nversion:1.2\nheart-beat:0,10000\n\n\u0000")
        testScheduler.runCurrent()

        // Advance time by 10s
        testScheduler.advanceTimeBy(10000)
        testScheduler.runCurrent()

        // Verify \n was sent as heartbeat
        verify(atLeast = 1) { mockWebSocket.send("\n") }

        client.disconnect()
        testScheduler.runCurrent()
    }
}
