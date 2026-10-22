package com.example.omnigo.core.websocket

import com.example.omnigo.core.datastore.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StompClientManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var managerScope: CoroutineScope

    private val mockOkHttpClient = mockk<OkHttpClient>(relaxed = true)
    private val mockSessionManager = mockk<SessionManager>()
    private val isLoggedInFlow = MutableStateFlow(false)

    private lateinit var manager: StompClientManager

    @Before
    fun setup() {
        managerScope = CoroutineScope(SupervisorJob() + testDispatcher)
        every { mockSessionManager.isLoggedInFlow } returns isLoggedInFlow
        coEvery { mockSessionManager.getUserId() } returns 42L
        coEvery { mockSessionManager.getAccessToken() } returns "mock-token-xyz"

        manager = StompClientManager(
            okHttpClient = mockOkHttpClient,
            sessionManager = mockSessionManager,
            scope = managerScope
        )
    }

    @After
    fun tearDown() {
        managerScope.cancel()
    }

    @Test
    fun `buildWebSocketUrl constructs pure websocket path correctly`() {
        val httpUrl = "http://api.omnigo.vn:8080"
        val bookingWs = StompClientManager.buildWebSocketUrl(httpUrl, "ws-booking")
        assertEquals("ws://api.omnigo.vn:8080/ws-booking/websocket", bookingWs)

        val httpsUrl = "https://api.omnigo.vn"
        val locationWs = StompClientManager.buildWebSocketUrl(httpsUrl, "ws-location")
        assertEquals("wss://api.omnigo.vn/ws-location/websocket", locationWs)
    }

    @Test
    fun `logging in triggers connectAll on both clients`() = testScope.runTest {
        advanceUntilIdle()

        // When user logs in
        isLoggedInFlow.value = true
        advanceUntilIdle()

        coVerify { mockSessionManager.getUserId() }
        coVerify { mockSessionManager.getAccessToken() }
    }

    @Test
    fun `logging out triggers disconnectAll on both clients`() = testScope.runTest {
        isLoggedInFlow.value = true
        advanceUntilIdle()

        // When user logs out
        isLoggedInFlow.value = false
        advanceUntilIdle()

        assertTrue(manager.bookingClient.connectionState.value is StompConnectionState.Disconnected)
        assertTrue(manager.locationClient.connectionState.value is StompConnectionState.Disconnected)
    }
}
