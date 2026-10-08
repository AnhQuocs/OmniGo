package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.driver.domain.repository.DriverRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateDriverStatusUseCaseTest {

    private lateinit var repository: DriverRepository
    private lateinit var useCase: UpdateDriverStatusUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UpdateDriverStatusUseCase(repository)
    }

    @Test
    fun `invoke with online true calls repository and returns success`() = runTest {
        coEvery { repository.updateDriverStatus(101L, true) } returns Result.success(true)

        val result = useCase(101L, true)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { repository.updateDriverStatus(101L, true) }
    }

    @Test
    fun `invoke with offline false calls repository and returns success`() = runTest {
        coEvery { repository.updateDriverStatus(101L, false) } returns Result.success(true)

        val result = useCase(101L, false)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { repository.updateDriverStatus(101L, false) }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { repository.updateDriverStatus(101L, true) } returns Result.failure(error)

        val result = useCase(101L, true)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
