package com.example.omnigo.features.customer.food.domain.usecase



import com.example.omnigo.features.customer.food.domain.error.FoodError

import com.example.omnigo.features.customer.food.domain.model.FoodOrder

import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem

import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult

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



class GetFoodOrderDetailUseCaseTest {



    private lateinit var repository: FoodRepository

    private lateinit var useCase: GetFoodOrderDetailUseCase



    private val mockOrder = FoodOrder(

        id = 999L,

        customerId = 10L,

        driverId = 55L,

        driverName = "Nguyễn Văn Shipper",

        driverPhone = "0912345678",

        driverVehiclePlate = "29A-12345",

        restaurantId = 1L,

        restaurantName = "Phở Thìn Lò Đúc",

        restaurantAddress = "13 Lò Đúc, Hai Bà Trưng",

        status = "DELIVERING",

        paymentMethod = PaymentMethod.CASH,

        isPaid = false,

        itemsPrice = 100000.0,

        deliveryFee = 15000.0,

        totalPrice = 115000.0,

        dropOffAddress = "123 Hoàng Cầu, Đống Đa, Hà Nội",

        dropOffLatitude = 21.0180,

        dropOffLongitude = 105.8560,

        note = "Gọi trước khi đến",

        items = listOf(

            FoodOrderItem(

                id = 1L,

                menuItemId = 101L,

                itemName = "Phở Tái",

                itemPrice = 50000.0,

                quantity = 2,

                subtotal = 100000.0,

                note = "Ít hành"

            )

        ),

        estimatedDeliveryMinutes = 15,

        createdAt = "2026-09-29T12:00:00Z"

    )



    @Before

    fun setUp() {

        repository = mockk()

        useCase = GetFoodOrderDetailUseCase(repository)

    }



    @Test

    fun `invoke with invalid orderId returns error`() = runTest {

        val result = useCase(0L)

        assertTrue(result is GetFoodOrderDetailResult.Error)

        assertEquals("Invalid order ID", (result as GetFoodOrderDetailResult.Error).message)



        val negativeResult = useCase(-5L)

        assertTrue(negativeResult is GetFoodOrderDetailResult.Error)
        coVerify(exactly = 0) { repository.getFoodOrderDetail(any()) }

    }



    @Test

    fun `invoke with valid orderId returns success from repository`() = runTest {

        coEvery { repository.getFoodOrderDetail(999L) } returns GetFoodOrderDetailResult.Success(mockOrder)



        val result = useCase(999L)

        assertTrue(result is GetFoodOrderDetailResult.Success)

        val success = result as GetFoodOrderDetailResult.Success

        assertEquals(999L, success.order.id)

        assertEquals("DELIVERING", success.order.status)

        assertEquals("Nguyễn Văn Shipper", success.order.driverName)

        assertEquals("Phở Thìn Lò Đúc", success.order.restaurantName)

        assertEquals(115000.0, success.order.totalPrice!!, 0.001)

    }



    @Test

    fun `invoke when repository returns error propagates error result`() = runTest {

        coEvery { repository.getFoodOrderDetail(999L) } returns GetFoodOrderDetailResult.Error(

            FoodError.SERVER_ERROR,

            "Order not found"

        )



        val result = useCase(999L)

        assertTrue(result is GetFoodOrderDetailResult.Error)

        val error = result as GetFoodOrderDetailResult.Error

        assertEquals(FoodError.SERVER_ERROR, error.error)

        assertEquals("Order not found", error.message)

    }

}
