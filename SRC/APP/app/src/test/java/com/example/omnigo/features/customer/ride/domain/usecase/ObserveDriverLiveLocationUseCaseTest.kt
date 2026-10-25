package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ObserveDriverLiveLocationUseCaseTest {

    private lateinit var repository: RideBookingRepository
    private lateinit var useCase: ObserveDriverLiveLocationUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = ObserveDriverLiveLocationUseCase(repository)
    }

    @Test
    fun `invoke with non-positive driverId returns empty flow without querying repository`() = runTest {
        val resultZero = useCase(0L).toList()
        val resultNegative = useCase(-5L).toList()

        assertTrue(resultZero.isEmpty())
        assertTrue(resultNegative.isEmpty())
        verify(exactly = 0) { repository.observeDriverLiveLocation(any()) }
    }

    @Test
    fun `invoke with positive driverId returns stream from repository`() = runTest {
        val sampleLocation = DriverLiveLocation(
            driverId = 123L,
            latitude = 10.7721,
            longitude = 106.6983,
            bearing = 45f,
            speed = 10f
        )
        every { repository.observeDriverLiveLocation(123L) } returns flowOf(sampleLocation)

        val result = useCase(123L).first()

        assertEquals(123L, result.driverId)
        assertEquals(10.7721, result.latitude, 0.0001)
        assertEquals(106.6983, result.longitude, 0.0001)
        assertEquals(45f, result.bearing ?: 0f, 0.1f)
        verify(exactly = 1) { repository.observeDriverLiveLocation(123L) }
    }
}
