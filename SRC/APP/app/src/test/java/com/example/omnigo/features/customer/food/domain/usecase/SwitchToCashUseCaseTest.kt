package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.SwitchToCashResult
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SwitchToCashUseCaseTest {

    private lateinit var repository: FoodRepository
    private lateinit var useCase: SwitchToCashUseCase

    private val mockUpdatedOrder = FoodOrder(
        id = 200L,
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
        estimatedDeliveryMinutes = 25,
        createdAt = "2026-09-29T12:00:00Z"
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SwitchToCashUseCase(repository)
    }

    @Test
    fun `invoke with invalid orderId returns error without repository call`() = runTest {
        val result = useCase(0L)

        assertTrue(result is SwitchToCashResult.Error)
        val error = result as SwitchToCashResult.Error
        assertEquals("Invalid order ID", error.message)
        coVerify(exactly = 0) { repository.switchToCash(any()) }
    }

    @Test
    fun `invoke with valid orderId returns success`() = runTest {
        coEvery { repository.switchToCash(200L) } returns SwitchToCashResult.Success(mockUpdatedOrder)

        val result = useCase(200L)

        assertTrue(result is SwitchToCashResult.Success)
        val success = result as SwitchToCashResult.Success
        assertEquals(200L, success.order.id)
        assertEquals(PaymentMethod.CASH, success.order.paymentMethod)
    }

    @Test
    fun `invoke when repository fails returns error`() = runTest {
        coEvery { repository.switchToCash(200L) } returns SwitchToCashResult.Error(
            FoodError.SERVER_ERROR,
            "Order is already paid"
        )

        val result = useCase(200L)

        assertTrue(result is SwitchToCashResult.Error)
        val error = result as SwitchToCashResult.Error
        assertEquals(FoodError.SERVER_ERROR, error.error)
        assertEquals("Order is already paid", error.message)
    }
}
