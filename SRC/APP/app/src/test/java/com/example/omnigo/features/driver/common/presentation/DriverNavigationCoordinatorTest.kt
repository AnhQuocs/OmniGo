package com.example.omnigo.features.driver.common.presentation

import com.example.omnigo.core.location.tracker.DriverNavigationLocationSource
import com.example.omnigo.core.location.tracker.OsrmRoutingService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverNavigationCoordinatorTest {

    private lateinit var osrmRoutingService: OsrmRoutingService
    private lateinit var navigationLocationSource: DriverNavigationLocationSource
    private var fakeNowMs: Long = 10_000L
    private lateinit var coordinator: DriverNavigationCoordinator

    private val sampleRoute = listOf(
        Pair(10.7600, 106.6600),
        Pair(10.7610, 106.6600),
        Pair(10.7620, 106.6600),
        Pair(10.7630, 106.6600)
    )

    @Before
    fun setUp() {
        osrmRoutingService = mockk()
        navigationLocationSource = mockk(relaxed = true)
        fakeNowMs = 10_000L
        coordinator = DriverNavigationCoordinator(
            osrmRoutingService = osrmRoutingService,
            navigationLocationSource = navigationLocationSource,
            clock = { fakeNowMs }
        )
    }

    @Test
    fun `updateDestination then onLocationTick fetches route and snaps polyline`() = runTest {
        coEvery {
            osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any())
        } returns sampleRoute

        coordinator.updateDestination(
            targetLat = 10.7630,
            targetLng = 106.6600,
            targetAddress = "123 Le Loi"
        )
        coordinator.onLocationTick(lat = 10.7605, lng = 106.6600, bearing = 0f)

        val state = coordinator.navigationState.value
        assertFalse(state.isRouteLoading)
        assertEquals("123 Le Loi", state.targetAddress)
        assertEquals(4, state.remainingPolyline.size)
        assertTrue(state.remainingDistanceMeters > 200.0)
        assertTrue(state.etaMinutes >= 1)
        coVerify(exactly = 1) {
            osrmRoutingService.getRouteCoordinates(10.7605, 106.6600, 10.7630, 106.6600, "BIKE")
        }
    }

    @Test
    fun `onLocationTick trims traversed segments as driver advances`() = runTest {
        coEvery {
            osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any())
        } returns sampleRoute

        coordinator.updateDestination(10.7630, 106.6600, "Destination")
        coordinator.onLocationTick(10.7602, 106.6600, 0f)
        assertEquals(4, coordinator.navigationState.value.remainingPolyline.size)

        // Advance to segment 2 (between 10.7620 and 10.7630)
        coordinator.onLocationTick(10.7625, 106.6600, 0f)
        val state = coordinator.navigationState.value
        assertEquals(2, state.remainingPolyline.size)
        assertEquals(10.7625, state.remainingPolyline[0].first, 0.0001)
        assertEquals(10.7630, state.remainingPolyline[1].first, 0.0001)
    }

    @Test
    fun `offRoute triggers auto-reroute only after 8 second cooldown`() = runTest {
        coEvery {
            osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any())
        } returns sampleRoute

        coordinator.updateDestination(10.7630, 106.6600, "Destination")
        coordinator.onLocationTick(10.7602, 106.6600, 0f)
        coVerify(exactly = 1) { osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any()) }

        // 3 seconds later (< 8s cooldown), driver deviates > 50m East -> should NOT reroute yet
        fakeNowMs += 3000L
        coordinator.onLocationTick(10.7610, 106.6610, 90f)
        coVerify(exactly = 1) { osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any()) }

        // 6 seconds later (total 9s >= 8s cooldown), still off-route -> SHOULD auto-reroute!
        fakeNowMs += 6000L
        coordinator.onLocationTick(10.7610, 106.6612, 90f)
        coVerify(exactly = 2) { osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `fallback to 2-point straight line when OsrmRoutingService returns empty`() = runTest {
        coEvery {
            osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any())
        } returns emptyList()

        coordinator.updateDestination(10.7603, 106.6600, "Short Target")
        coordinator.onLocationTick(10.7600, 106.6600, 0f)

        val state = coordinator.navigationState.value
        assertEquals(2, state.remainingPolyline.size)
        assertEquals(10.7600, state.remainingPolyline.first().first, 0.0001)
        assertEquals(10.7603, state.remainingPolyline.last().first, 0.0001)
    }
}
