package com.example.omnigo.features.customer.home.domain.usecase

import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.location.tracker.LocationTracker
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DetectCurrentLocationUseCaseTest {

    private lateinit var locationTracker: LocationTracker
    private lateinit var useCase: DetectCurrentLocationUseCase

    @Before
    fun setUp() {
        locationTracker = mockk()
        useCase = DetectCurrentLocationUseCase(locationTracker)
    }

    @Test
    fun `invoke when permission not granted returns PermissionRequired`() = runTest {
        every { locationTracker.isLocationPermissionGranted() } returns false

        val result = useCase()

        assertTrue(result is DetectLocationResult.PermissionRequired)
    }

    @Test
    fun `invoke when gps is disabled returns GpsDisabled`() = runTest {
        every { locationTracker.isLocationPermissionGranted() } returns true
        every { locationTracker.isGpsEnabled() } returns false

        val result = useCase()

        assertTrue(result is DetectLocationResult.GpsDisabled)
    }

    @Test
    fun `invoke when gps enabled and location found returns Success with location`() = runTest {
        val mockLocation = UserLocation(
            latitude = 21.0285,
            longitude = 105.8542,
            addressName = "Hoàn Kiếm, Hà Nội",
            fullAddress = "Phố Tràng Tiền, Quận Hoàn Kiếm, Hà Nội"
        )
        every { locationTracker.isLocationPermissionGranted() } returns true
        every { locationTracker.isGpsEnabled() } returns true
        coEvery { locationTracker.getCurrentLocation() } returns mockLocation

        val result = useCase()

        assertTrue(result is DetectLocationResult.Success)
        assertEquals(mockLocation, (result as DetectLocationResult.Success).location)
    }

    @Test
    fun `invoke when tracker returns null returns LocationUnavailable`() = runTest {
        every { locationTracker.isLocationPermissionGranted() } returns true
        every { locationTracker.isGpsEnabled() } returns true
        coEvery { locationTracker.getCurrentLocation() } returns null

        val result = useCase()

        assertTrue(result is DetectLocationResult.LocationUnavailable)
    }
}
