package com.example.omnigo.features.driver.food.domain.usecase

import com.example.omnigo.features.driver.domain.repository.DriverRepository
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDriverActiveFoodOrdersUseCaseTest {

    private lateinit var repository: DriverRepository
    private lateinit var useCase: GetDriverActiveFoodOrdersUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetDriverActiveFoodOrdersUseCase(repository)
    }

    @Test
    fun `invoke calls repository getDriverActiveFoodOrders and returns list`() = runTest {
        val mockOrder = DriverDeliveryOrder(
            orderId = 111L,
            status = DriverDeliveryStatus.DELIVERING,
            restaurantName = "Quan Pho",
            restaurantAddress = "123 Pho Hue",
            dropOffAddress = "456 Tran Hung Dao",
            totalPrice = 50000.0,
            deliveryFee = 15000.0,
            isPaid = true
        )
        coEvery { repository.getDriverActiveFoodOrders() } returns Result.success(listOf(mockOrder))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(111L, result.getOrNull()?.first()?.orderId)
        coVerify(exactly = 1) { repository.getDriverActiveFoodOrders() }
    }

    @Test
    fun `invoke propagates failure when repository fails`() = runTest {
        coEvery { repository.getDriverActiveFoodOrders() } returns Result.failure(RuntimeException("Network failure"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Network failure", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { repository.getDriverActiveFoodOrders() }
    }
}
