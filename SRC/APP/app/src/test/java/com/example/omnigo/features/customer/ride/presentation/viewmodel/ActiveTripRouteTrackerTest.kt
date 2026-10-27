package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.core.location.tracker.OsrmRoutingService
import com.example.omnigo.features.customer.ride.presentation.state.RideBookingUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveTripRouteTrackerTest {

    private lateinit var osrmRoutingService: OsrmRoutingService
    private lateinit var tracker: ActiveTripRouteTracker
    private var uiState = RideBookingUiState()

    private val routeToPickup = listOf(21.018 to 105.848, 21.019 to 105.849, 21.020 to 105.850)
    private val routeToDropoff = listOf(21.020 to 105.850, 21.030 to 105.860)

    @Before
    fun setUp() {
        osrmRoutingService = mockk()
        tracker = ActiveTripRouteTracker(osrmRoutingService)
        uiState = RideBookingUiState()
    }

    private fun updateState(transform: (RideBookingUiState) -> RideBookingUiState) {
        uiState = transform(uiState)
    }

    private fun TestScopeHelper.send(lat: Double, lng: Double, phase: TripRoutePhase, tLat: Double, tLng: Double) =
        tracker.onDriverLocation(scope, lat, lng, phase, tLat, tLng, "BIKE", ::updateState)

    private class TestScopeHelper(val scope: kotlinx.coroutines.CoroutineScope)

    @Test
    fun `first driver location fetches driver to pickup route and publishes it`() = runTest {
        coEvery { osrmRoutingService.getRouteCoordinates(21.018, 105.848, 21.02, 105.85, "BIKE") } returns routeToPickup

        TestScopeHelper(this).send(21.018, 105.848, TripRoutePhase.TO_PICKUP, 21.02, 105.85)
        advanceUntilIdle()

        assertEquals(routeToPickup, uiState.activeTripRouteCoordinates)
    }

    @Test
    fun `small movement does not refetch but 150m or more does`() = runTest {
        coEvery { osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any()) } returns routeToPickup
        val helper = TestScopeHelper(this)

        helper.send(21.0180, 105.8480, TripRoutePhase.TO_PICKUP, 21.02, 105.85)
        advanceUntilIdle()
        helper.send(21.0181, 105.8481, TripRoutePhase.TO_PICKUP, 21.02, 105.85) // ~15m
        advanceUntilIdle()
        coVerify(exactly = 1) { osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any()) }

        helper.send(21.0200, 105.8480, TripRoutePhase.TO_PICKUP, 21.02, 105.85) // ~220m
        advanceUntilIdle()
        coVerify(exactly = 2) { osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `phase change refetches immediately even without movement`() = runTest {
        coEvery { osrmRoutingService.getRouteCoordinates(21.02, 105.85, 21.03, 105.86, "BIKE") } returns routeToDropoff
        coEvery { osrmRoutingService.getRouteCoordinates(21.02, 105.85, 21.02, 105.85, "BIKE") } returns routeToPickup
        val helper = TestScopeHelper(this)

        helper.send(21.02, 105.85, TripRoutePhase.TO_PICKUP, 21.02, 105.85)
        advanceUntilIdle()
        helper.send(21.02, 105.85, TripRoutePhase.TO_DROPOFF, 21.03, 105.86)
        advanceUntilIdle()

        assertEquals(routeToDropoff, uiState.activeTripRouteCoordinates)
    }

    @Test
    fun `empty route response leaves previous route untouched`() = runTest {
        uiState = uiState.copy(activeTripRouteCoordinates = routeToPickup)
        coEvery { osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any()) } returns emptyList()

        TestScopeHelper(this).send(21.018, 105.848, TripRoutePhase.TO_PICKUP, 21.02, 105.85)
        advanceUntilIdle()

        assertEquals(routeToPickup, uiState.activeTripRouteCoordinates)
    }

    @Test
    fun `reset forces a fresh fetch for the same phase`() = runTest {
        coEvery { osrmRoutingService.getRouteCoordinates(any(), any(), any(), any(), any()) } returns routeToPickup
        val helper = TestScopeHelper(this)

        helper.send(21.018, 105.848, TripRoutePhase.TO_PICKUP, 21.02, 105.85)
        advanceUntilIdle()
        assertFalse(tracker.shouldRefetch(TripRoutePhase.TO_PICKUP, 21.018, 105.848))

        tracker.reset()
        assertTrue(tracker.shouldRefetch(TripRoutePhase.TO_PICKUP, 21.018, 105.848))
    }
}
