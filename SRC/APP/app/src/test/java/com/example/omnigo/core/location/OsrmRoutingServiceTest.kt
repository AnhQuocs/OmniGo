package com.example.omnigo.core.location

import android.content.Context
import android.util.Log
import com.example.omnigo.core.location.tracker.OsrmRoutingService
import com.example.omnigo.features.customer.ride.data.remote.api.PricingApi
import com.example.omnigo.features.customer.ride.data.remote.dto.RouteResultDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OsrmRoutingServiceTest {

    private lateinit var context: Context
    private lateinit var pricingApi: PricingApi
    private lateinit var service: OsrmRoutingService

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0

        context = mockk(relaxed = true)
        pricingApi = mockk(relaxed = true)
        service = OsrmRoutingService(context, pricingApi)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `getRouteCoordinates returns empty list when start coordinates are zeros`() = runTest {
        val result = service.getRouteCoordinates(0.0, 0.0, 21.0285, 105.8542)
        assertEquals(emptyList<Pair<Double, Double>>(), result)
    }

    @Test
    fun `getRouteCoordinates returns empty list when end coordinates are zeros`() = runTest {
        val result = service.getRouteCoordinates(21.0285, 105.8542, 0.0, 0.0)
        assertEquals(emptyList<Pair<Double, Double>>(), result)
    }

    @Test
    fun `getRouteCoordinates decodes polyline from backend response`() = runTest {
        // Sample polyline "_p~iF~ps|U_ulLnnqC_mqNvxq`@" has 3 points
        coEvery { pricingApi.getRoute(any()) } returns RouteResultDto(
            distanceKm = 10.5,
            durationMinutes = 25,
            encodedPolyline = "_p~iF~ps|U_ulLnnqC_mqNvxq`@",
            source = "OSRM_LOCAL"
        )

        val result = service.getRouteCoordinates(21.0285, 105.8542, 21.0100, 105.8500)
        assertEquals(3, result.size)
        assertEquals(38.5, result[0].first, 0.001)
        assertEquals(-120.2, result[0].second, 0.001)
    }
}
