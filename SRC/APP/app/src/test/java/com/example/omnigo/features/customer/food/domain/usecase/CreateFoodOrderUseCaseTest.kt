package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.CartItem
import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderCreateCommand
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateFoodOrderUseCaseTest {

    private lateinit var repository: FoodRepository
    private lateinit var useCase: CreateFoodOrderUseCase

    private val mockMenuItem = MenuItem(
        id = 101L,
        restaurantId = 1L,
        name = "Phở Tái",
        description = "Bò tái tươi",
        price = 50000.0,
        imageUrl = "https://example.com/pho.jpg",
        category = "Phở",
        isAvailable = true
    )

    private val mockCartItem = CartItem(
        menuItem = mockMenuItem,
        quantity = 2,
        note = "Ít hành"
    )

    private val mockOrder = FoodOrder(
        id = 1001L,
        customerId = 12L,
        driverId = null,
        restaurantId = 1L,
        restaurantName = "Phở Thìn",
        status = "PENDING",
        paymentMethod = PaymentMethod.CASH,
        isPaid = false,
        itemsPrice = 100000.0,
        deliveryFee = 15000.0,
        totalPrice = 115000.0,
        dropOffAddress = "123 Hoàng Cầu, Đống Đa, Hà Nội",
        dropOffLatitude = 21.0180,
        dropOffLongitude = 105.8560,
        note = "Gọi trước khi đến",
        items = emptyList(),
        estimatedDeliveryMinutes = 25,
        createdAt = "2026-09-29T12:00:00Z"
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = CreateFoodOrderUseCase(repository)
    }

    @Test
    fun `invoke with empty items returns error`() = runTest {
        val result = useCase(
            restaurantId = 1L,
            dropOffAddress = "123 Hoàng Cầu",
            dropOffLatitude = 21.0,
            dropOffLongitude = 105.0,
            items = emptyList(),
            paymentMethod = PaymentMethod.CASH
        )

        assertTrue(result is CreateFoodOrderResult.Error)
        assertEquals("Cart is empty", (result as CreateFoodOrderResult.Error).message)
    }

    @Test
    fun `invoke with blank dropOffAddress returns error`() = runTest {
        val result = useCase(
            restaurantId = 1L,
            dropOffAddress = "   ",
            dropOffLatitude = 21.0,
            dropOffLongitude = 105.0,
            items = listOf(mockCartItem),
            paymentMethod = PaymentMethod.CASH
        )

        assertTrue(result is CreateFoodOrderResult.Error)
        assertEquals("Delivery address is required", (result as CreateFoodOrderResult.Error).message)
    }

    @Test
    fun `invoke with valid parameters calls repository and returns success`() = runTest {
        coEvery { repository.createFoodOrder(any()) } returns CreateFoodOrderResult.Success(mockOrder)

        val result = useCase(
            restaurantId = 1L,
            dropOffAddress = "123 Hoàng Cầu, Đống Đa, Hà Nội",
            dropOffLatitude = 21.0180,
            dropOffLongitude = 105.8560,
            items = listOf(mockCartItem),
            note = "Gọi trước khi đến",
            paymentMethod = PaymentMethod.CASH
        )

        assertTrue(result is CreateFoodOrderResult.Success)
        val success = result as CreateFoodOrderResult.Success
        assertEquals(1001L, success.order.id)
        assertEquals("PENDING", success.order.status)
        assertEquals(115000.0, success.order.totalPrice!!, 0.001)
    }

    @Test
    fun `invoke when repository returns error propagates error result`() = runTest {
        coEvery { repository.createFoodOrder(any()) } returns CreateFoodOrderResult.Error(FoodError.SERVER_ERROR, "Server busy")

        val result = useCase(
            restaurantId = 1L,
            dropOffAddress = "123 Hoàng Cầu",
            dropOffLatitude = 21.0,
            dropOffLongitude = 105.0,
            items = listOf(mockCartItem),
            paymentMethod = PaymentMethod.WALLET
        )

        assertTrue(result is CreateFoodOrderResult.Error)
        assertEquals(FoodError.SERVER_ERROR, (result as CreateFoodOrderResult.Error).error)
        assertEquals("Server busy", (result as CreateFoodOrderResult.Error).message)
    }

    @Test
    fun `invoke maps validated input into a domain create command`() = runTest {
        val slot = io.mockk.slot<FoodOrderCreateCommand>()
        coEvery { repository.createFoodOrder(capture(slot)) } returns CreateFoodOrderResult.Success(mockOrder)

        useCase(
            restaurantId = 99L,
            dropOffAddress = "456 Kim Mã, Ba Đình",
            dropOffLatitude = 21.0310,
            dropOffLongitude = 105.8190,
            items = listOf(mockCartItem),
            note = "Không cho ớt",
            paymentMethod = PaymentMethod.MOMO
        )

        val req = slot.captured
        assertEquals(99L, req.restaurantId)
        assertEquals("456 Kim Mã, Ba Đình", req.dropOffAddress)
        assertEquals(21.0310, req.dropOffLatitude, 0.0001)
        assertEquals(105.8190, req.dropOffLongitude, 0.0001)
        assertEquals("Không cho ớt", req.note)
        assertEquals(PaymentMethod.MOMO, req.paymentMethod)
        assertEquals(1, req.items.size)
        assertEquals(101L, req.items[0].menuItemId)
        assertEquals(2, req.items[0].quantity)
        assertEquals("Ít hành", req.items[0].note)
    }
}
