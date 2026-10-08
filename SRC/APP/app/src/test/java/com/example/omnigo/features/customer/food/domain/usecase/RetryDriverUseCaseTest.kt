package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.RetryDriverResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RetryDriverUseCaseTest {

    private lateinit var repository: FoodRepository
    private lateinit var useCase: RetryDriverUseCase

    private val mockOrder = FoodOrder(
        id = 300L,
        customerId = 10L,
        driverId = null,
        restaurantId = 1L,
        restaurantName = "Phở Thìn Lò Đúc",
        restaurantAddress = "13 Lò Đúc, Hà Nội",
        status = "PENDING",
        paymentMethod = PaymentMethod.CASH,
        isPaid = false,
        itemsPrice = 100000.0,
        deliveryFee = 15000.0,
        totalPrice = 115000.0,
        dropOffAddress = "123 Hoàng Cầu",
        dropOffLatitude = 21.0180,
        dropOffLongitude = 105.8560,
        note = "",
        items = emptyList(),
        estimatedDeliveryMinutes = 20,
        createdAt = "2026-09-29T12:00:00Z"
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RetryDriverUseCase(repository)
    }

    @Test
    fun `invoke with invalid orderId returns error without repository call`() = runTest {
        val result = useCase(0L)

        assertTrue(result is RetryDriverResult.Error)
        val error = result as RetryDriverResult.Error
        assertEquals("Invalid order ID", error.message)
        coVerify(exactly = 0) { repository.retryDriver(any()) }
    }

    @Test
    fun `invoke with valid orderId returns success`() = runTest {
        coEvery { repository.retryDriver(300L) } returns RetryDriverResult.Success(mockOrder)

        val result = useCase(300L)

        assertTrue(result is RetryDriverResult.Success)
        val success = result as RetryDriverResult.Success
        assertEquals(300L, success.order.id)
    }

    @Test
    fun `invoke when repository fails returns error`() = runTest {
        coEvery { repository.retryDriver(300L) } returns RetryDriverResult.Error(
            FoodError.SERVER_ERROR,
            "Max retries reached"
        )

        val result = useCase(300L)

        assertTrue(result is RetryDriverResult.Error)
        val error = result as RetryDriverResult.Error
        assertEquals(FoodError.SERVER_ERROR, error.error)
        assertEquals("Max retries reached", error.message)
    }
}
