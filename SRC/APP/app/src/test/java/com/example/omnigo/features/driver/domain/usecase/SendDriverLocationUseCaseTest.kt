package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.driver.domain.model.DriverLocationUpdate
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SendDriverLocationUseCaseTest {

    private lateinit var repository: DriverRepository
    private lateinit var useCase: SendDriverLocationUseCase

    private val sampleUpdate = DriverLocationUpdate(
        latitude = 21.0285,
        longitude = 105.8542,
        bearing = 90.0f,
        speed = 15.5f,
        timestamp = 1690000000000L
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SendDriverLocationUseCase(repository)
    }

    @Test
    fun `invoke sends location update successfully`() = runTest {
        coEvery { repository.sendDriverLocation(sampleUpdate) } returns Result.success(true)

        val result = useCase(sampleUpdate)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { repository.sendDriverLocation(sampleUpdate) }
    }

    @Test
    fun `invoke returns failure when repository fails to push location`() = runTest {
        val error = RuntimeException("Location service unreachable")
        coEvery { repository.sendDriverLocation(sampleUpdate) } returns Result.failure(error)

        val result = useCase(sampleUpdate)

        assertTrue(result.isFailure)
        assertEquals("Location service unreachable", result.exceptionOrNull()?.message)
    }
}
