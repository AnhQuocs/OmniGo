package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.location.tracker.LocationTracker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchPlacesUseCaseTest {

    private lateinit var locationTracker: LocationTracker
    private lateinit var useCase: SearchPlacesUseCase

    @Before
    fun setUp() {
        locationTracker = mockk()
        useCase = SearchPlacesUseCase(locationTracker)
    }

    @Test
    fun `invoke with query shorter than 2 chars returns empty list without calling tracker`() = runTest {
        val result = useCase("a")

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { locationTracker.searchPlaces(any()) }
    }

    @Test
    fun `invoke with whitespace query returns empty list without calling tracker`() = runTest {
        val result = useCase("   ")

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { locationTracker.searchPlaces(any()) }
    }

    @Test
    fun `invoke with valid query calls tracker and returns mapped LocationPoints`() = runTest {
        val mockLocations = listOf(
            UserLocation(
                latitude = 21.0285,
                longitude = 105.8542,
                addressName = "Hanoi Opera House",
                fullAddress = "1 Trang Tien, Hoan Kiem, Hanoi"
            ),
            UserLocation(
                latitude = 21.0333,
                longitude = 105.8433,
                addressName = "Hoan Kiem Lake",
                fullAddress = "Dinh Tien Hoang, Hoan Kiem, Hanoi"
            )
        )
        coEvery { locationTracker.searchPlaces("Hanoi") } returns mockLocations

        val result = useCase("  Hanoi  ")

        assertEquals(2, result.size)
        assertEquals("Hanoi Opera House", result[0].name)
        assertEquals("1 Trang Tien, Hoan Kiem, Hanoi", result[0].address)
        assertEquals(21.0285, result[0].latitude, 0.0001)
        assertEquals(105.8542, result[0].longitude, 0.0001)

        assertEquals("Hoan Kiem Lake", result[1].name)
        assertEquals(21.0333, result[1].latitude, 0.0001)

        coVerify(exactly = 1) { locationTracker.searchPlaces("Hanoi") }
    }

    @Test
    fun `invoke when tracker returns empty list returns empty list`() = runTest {
        coEvery { locationTracker.searchPlaces("UnknownPlace") } returns emptyList()

        val result = useCase("UnknownPlace")

        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { locationTracker.searchPlaces("UnknownPlace") }
    }
}
