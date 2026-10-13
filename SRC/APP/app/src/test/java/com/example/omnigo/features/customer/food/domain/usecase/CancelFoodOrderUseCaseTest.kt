package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.CancelFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CancelFoodOrderUseCaseTest {

    private lateinit var repository: FoodRepository
    private lateinit var useCase: CancelFoodOrderUseCase

    private val mockCancelledOrder = FoodOrder(
        id = 100L,
        customerId = 10L,
        driverId = null,
        restaurantId = 1L,
        restaurantName = "Phở Thìn Lò Đúc",
        restaurantAddress = "13 Lò Đúc, Hà Nội",
        status = "CANCELLED",
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
        estimatedDeliveryMinutes = null,
        createdAt = "2026-09-29T12:00:00Z"
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = CancelFoodOrderUseCase(repository)
    }

    @Test
    fun `invoke with invalid orderId returns error without repository call`() = runTest {
        val result = useCase(0L, "CHANGED_MIND", "Changed mind")

        assertTrue(result is CancelFoodOrderResult.Error)
        val error = result as CancelFoodOrderResult.Error
        assertEquals("Invalid order ID", error.message)
        coVerify(exactly = 0) { repository.cancelFoodOrder(any(), any(), any()) }
    }

    @Test
    fun `invoke with valid parameters returns success`() = runTest {
        coEvery {
            repository.cancelFoodOrder(100L, "CHANGED_MIND", "I changed my mind")
        } returns CancelFoodOrderResult.Success(mockCancelledOrder)

        val result = useCase(100L, "CHANGED_MIND", "I changed my mind")

        assertTrue(result is CancelFoodOrderResult.Success)
        val success = result as CancelFoodOrderResult.Success
        assertEquals(100L, success.order.id)
        assertEquals("CANCELLED", success.order.status)
    }

    @Test
    fun `invoke when repository fails returns error`() = runTest {
        coEvery {
            repository.cancelFoodOrder(100L, "CHANGED_MIND", null)
        } returns CancelFoodOrderResult.Error(FoodError.SERVER_ERROR, "Cannot cancel prepared order")

        val result = useCase(100L, "CHANGED_MIND", null)

        assertTrue(result is CancelFoodOrderResult.Error)
        val error = result as CancelFoodOrderResult.Error
        assertEquals(FoodError.SERVER_ERROR, error.error)
        assertEquals("Cannot cancel prepared order", error.message)
    }
}
