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

class GetDriverFoodOrderDetailUseCaseTest {

    private lateinit var repository: DriverRepository
    private lateinit var useCase: GetDriverFoodOrderDetailUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetDriverFoodOrderDetailUseCase(repository)
    }

    @Test
    fun `invoke with non-positive orderId returns failure without calling repository`() = runTest {
        val resultZero = useCase(0L)
        assertTrue(resultZero.isFailure)
        assertEquals("Invalid orderId: 0", resultZero.exceptionOrNull()?.message)

        val resultNegative = useCase(-5L)
        assertTrue(resultNegative.isFailure)
        assertEquals("Invalid orderId: -5", resultNegative.exceptionOrNull()?.message)

        coVerify(exactly = 0) { repository.getDriverFoodOrderDetail(any()) }
    }

    @Test
    fun `invoke with positive orderId calls repository and returns result`() = runTest {
        val mockOrder = DriverDeliveryOrder(
            orderId = 123L,
            status = DriverDeliveryStatus.ACCEPTED,
            restaurantName = "Phở Hà Nội",
            restaurantAddress = "123 Đường A",
            restaurantLatitude = 10.762,
            restaurantLongitude = 106.660,
            customerName = "Nguyễn Văn A",
            customerPhone = "0901234567",
            dropOffAddress = "456 Đường B",
            dropOffLatitude = 10.770,
            dropOffLongitude = 106.670,
            items = emptyList(),
            totalPrice = 120000.0,
            deliveryFee = 20000.0,
            isPaid = false
        )
        coEvery { repository.getDriverFoodOrderDetail(123L) } returns Result.success(mockOrder)

        val result = useCase(123L)

        assertTrue(result.isSuccess)
        assertEquals(mockOrder, result.getOrNull())
        coVerify(exactly = 1) { repository.getDriverFoodOrderDetail(123L) }
    }

    @Test
    fun `invoke with positive orderId propagates repository error`() = runTest {
        coEvery { repository.getDriverFoodOrderDetail(456L) } returns Result.failure(RuntimeException("Network error"))

        val result = useCase(456L)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { repository.getDriverFoodOrderDetail(456L) }
    }
}
