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

class AcceptFoodOrderUseCaseTest {

    private lateinit var driverRepository: DriverRepository
    private lateinit var useCase: AcceptFoodOrderUseCase

    @Before
    fun setUp() {
        driverRepository = mockk()
        useCase = AcceptFoodOrderUseCase(driverRepository)
    }

    @Test
    fun `invoke with valid orderId calls repository and returns success`() = runTest {
        val orderId = 100L
        coEvery { driverRepository.acceptFoodOrder(orderId) } returns Result.success(true)

        val result = useCase(orderId)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { driverRepository.acceptFoodOrder(orderId) }
    }

    @Test
    fun `invoke with invalid orderId returns failure without calling repository`() = runTest {
        val resultNegative = useCase(-1L)
        assertTrue(resultNegative.isFailure)

        val resultZero = useCase(0L)
        assertTrue(resultZero.isFailure)

        coVerify(exactly = 0) { driverRepository.acceptFoodOrder(any()) }
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val orderId = 100L
        coEvery { driverRepository.acceptFoodOrder(orderId) } returns Result.failure(RuntimeException("Conflict"))

        val result = useCase(orderId)

        assertTrue(result.isFailure)
        assertEquals("Conflict", result.exceptionOrNull()?.message)
    }
}
