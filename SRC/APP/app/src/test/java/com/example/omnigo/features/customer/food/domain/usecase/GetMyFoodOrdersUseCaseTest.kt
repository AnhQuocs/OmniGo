package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
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

class GetMyFoodOrdersUseCaseTest {

    private lateinit var repository: FoodRepository
    private lateinit var useCase: GetMyFoodOrdersUseCase

    private val mockOrders = listOf(
        FoodOrder(
            id = 101L,
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
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetMyFoodOrdersUseCase(repository)
    }

    @Test
    fun `invoke returns success from repository`() = runTest {
        coEvery { repository.getMyFoodOrders() } returns GetMyFoodOrdersResult.Success(mockOrders)

        val result = useCase()

        assertTrue(result is GetMyFoodOrdersResult.Success)
        val success = result as GetMyFoodOrdersResult.Success
        assertEquals(1, success.orders.size)
        assertEquals(101L, success.orders[0].id)
        coVerify(exactly = 1) { repository.getMyFoodOrders() }
    }

    @Test
    fun `invoke when repository returns error propagates error result`() = runTest {
        coEvery { repository.getMyFoodOrders() } returns GetMyFoodOrdersResult.Error(
            FoodError.SERVER_ERROR,
            "Failed to fetch orders"
        )

        val result = useCase()

        assertTrue(result is GetMyFoodOrdersResult.Error)
        val error = result as GetMyFoodOrdersResult.Error
        assertEquals(FoodError.SERVER_ERROR, error.error)
        assertEquals("Failed to fetch orders", error.message)
    }
}

