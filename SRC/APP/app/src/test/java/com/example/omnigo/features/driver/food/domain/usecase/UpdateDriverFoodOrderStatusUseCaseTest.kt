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

class UpdateDriverFoodOrderStatusUseCaseTest {

    private lateinit var repository: DriverRepository
    private lateinit var useCase: UpdateDriverFoodOrderStatusUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UpdateDriverFoodOrderStatusUseCase(repository)
    }

    @Test
    fun `invoke with invalid orderId returns failure`() = runTest {
        val result = useCase(orderId = 0L, status = "DELIVERING")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid orderId") == true)
        coVerify(exactly = 0) { repository.updateDriverOrderStatus(any(), any(), any(), any()) }
    }

    @Test
    fun `invoke with invalid target status returns failure`() = runTest {
        val result = useCase(orderId = 10L, status = "ACCEPTED")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid status") == true)
        coVerify(exactly = 0) { repository.updateDriverOrderStatus(any(), any(), any(), any()) }
    }

    @Test
    fun `invoke with DELIVERING calls repository with DELIVERING status`() = runTest {
        val mockOrder = DriverDeliveryOrder(
            orderId = 10L,
            status = DriverDeliveryStatus.DELIVERING,
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
        coEvery {
            repository.updateDriverOrderStatus(10L, "DELIVERING", null, null)
        } returns Result.success(mockOrder)

        val result = useCase(orderId = 10L, status = "DELIVERING")

        assertTrue(result.isSuccess)
        assertEquals(DriverDeliveryStatus.DELIVERING, result.getOrNull()?.status)
        coVerify(exactly = 1) {
            repository.updateDriverOrderStatus(10L, "DELIVERING", null, null)
        }
    }

    @Test
    fun `invoke with COMPLETED calls repository with COMPLETED status`() = runTest {
        val mockOrder = DriverDeliveryOrder(
            orderId = 10L,
            status = DriverDeliveryStatus.COMPLETED,
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
        coEvery {
            repository.updateDriverOrderStatus(10L, "COMPLETED", null, null)
        } returns Result.success(mockOrder)

        val result = useCase(orderId = 10L, status = "COMPLETED")

        assertTrue(result.isSuccess)
        assertEquals(DriverDeliveryStatus.COMPLETED, result.getOrNull()?.status)
        coVerify(exactly = 1) {
            repository.updateDriverOrderStatus(10L, "COMPLETED", null, null)
        }
    }
}
