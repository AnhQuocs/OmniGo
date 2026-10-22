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

class RejectFoodOrderUseCaseTest {

    private lateinit var driverRepository: DriverRepository
    private lateinit var useCase: RejectFoodOrderUseCase

    @Before
    fun setUp() {
        driverRepository = mockk()
        useCase = RejectFoodOrderUseCase(driverRepository)
    }

    @Test
    fun `invoke with valid orderId calls repository and returns success`() = runTest {
        val orderId = 100L
        coEvery { driverRepository.rejectFoodOrder(orderId) } returns Result.success(true)

        val result = useCase(orderId)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { driverRepository.rejectFoodOrder(orderId) }
    }

    @Test
    fun `invoke with invalid orderId returns failure without calling repository`() = runTest {
        val resultNegative = useCase(-1L)
        assertTrue(resultNegative.isFailure)

        val resultZero = useCase(0L)
        assertTrue(resultZero.isFailure)

        coVerify(exactly = 0) { driverRepository.rejectFoodOrder(any()) }
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val orderId = 100L
        coEvery { driverRepository.rejectFoodOrder(orderId) } returns Result.failure(RuntimeException("Network error"))

        val result = useCase(orderId)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
