package com.example.omnigo.features.driver.data.repository

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.driver.data.remote.api.DriverApi
import com.example.omnigo.features.driver.data.remote.dto.DriverAccountProfileResponse
import com.example.omnigo.features.driver.data.remote.dto.DriverLocationRequest
import com.example.omnigo.features.driver.data.remote.dto.DriverStatusUpdateRequest
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.model.DriverLocationUpdate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DriverRepositoryImplTest {

    private lateinit var driverApi: DriverApi
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: DriverRepositoryImpl

    @Before
    fun setUp() {
        driverApi = mockk()
        sessionManager = mockk()
        repository = DriverRepositoryImpl(driverApi, sessionManager)
    }

    @Test
    fun `updateDriverStatus with driverId calls updateDriverStatus on api`() = runTest {
        val successResponse: ApiResponse<String> = ApiResponse(
            success = true,
            message = "OK",
            data = "OK",
            timestamp = null
        )
        coEvery { driverApi.updateDriverStatus(101L, true) } returns successResponse

        val result = repository.updateDriverStatus(101L, true)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { driverApi.updateDriverStatus(101L, true) }
    }

    @Test
    fun `updateDriverStatus with null driverId queries sessionManager and fails when userId is null`() = runTest {
        coEvery { sessionManager.getUserId() } returns null

        val result = repository.updateDriverStatus(null, false)

        assertTrue(result.isFailure)
        assertEquals("Driver ID not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getDriverProfile parses response into DriverAccountProfile`() = runTest {
        val dto = DriverAccountProfileResponse(
            id = 101L,
            fullName = "Le Van Driver",
            phoneNumber = "0987654321",
            vehiclePlate = "51F-12345",
            status = "APPROVED",
            isOnline = true
        )
        val apiResponse = ApiResponse(
            success = true,
            message = "OK",
            data = dto,
            timestamp = null
        )
        coEvery { driverApi.getDriverProfileById(101L) } returns apiResponse

        val result = repository.getDriverProfile(101L)

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals(101L, profile.driverId)
        assertEquals("Le Van Driver", profile.fullName)
        assertEquals(DriverApprovalStatus.APPROVED, profile.approvalStatus)
        assertEquals(true, profile.isOnline)
    }

    @Test
    fun `sendDriverLocation constructs request and calls api`() = runTest {
        val update = DriverLocationUpdate(
            latitude = 21.0285,
            longitude = 105.8542,
            bearing = 45f,
            speed = 10f,
            timestamp = 1690000000000L
        )
        val request = DriverLocationRequest(
            latitude = 21.0285,
            longitude = 105.8542,
            bearing = 45f,
            speed = 10f,
            timestamp = 1690000000000L
        )
        val apiResponse: ApiResponse<Any> = ApiResponse(
            success = true,
            message = "Location updated",
            data = "OK",
            timestamp = null
        )
        coEvery { driverApi.sendDriverLocation(request) } returns apiResponse

        val result = repository.sendDriverLocation(update)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { driverApi.sendDriverLocation(request) }
    }

    @Test
    fun `acceptFoodOrder returns success when api responds with success true`() = runTest {
        val orderId = 202L
        val apiResponse = ApiResponse(
            success = true,
            message = "OK",
            data = "OK",
            timestamp = null
        )
        coEvery { driverApi.acceptFoodOrder(orderId) } returns apiResponse

        val result = repository.acceptFoodOrder(orderId)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { driverApi.acceptFoodOrder(orderId) }
    }

    @Test
    fun `acceptFoodOrder returns failure when api responds with success false`() = runTest {
        val orderId = 202L
        val apiResponse: ApiResponse<String> = ApiResponse(
            success = false,
            message = "Order already accepted by another driver",
            data = null,
            timestamp = null
        )
        coEvery { driverApi.acceptFoodOrder(orderId) } returns apiResponse

        val result = repository.acceptFoodOrder(orderId)

        assertTrue(result.isFailure)
        assertEquals("Order already accepted by another driver", result.exceptionOrNull()?.message)
    }

    @Test
    fun `acceptFoodOrder returns failure when api throws exception`() = runTest {
        val orderId = 202L
        coEvery { driverApi.acceptFoodOrder(orderId) } throws RuntimeException("Network error")

        val result = repository.acceptFoodOrder(orderId)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `rejectFoodOrder returns success when api responds with success true`() = runTest {
        val orderId = 202L
        val apiResponse: ApiResponse<String> = ApiResponse(
            success = true,
            message = "OK",
            data = "OK",
            timestamp = null
        )
        coEvery { driverApi.rejectFoodOrder(orderId) } returns apiResponse

        val result = repository.rejectFoodOrder(orderId)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { driverApi.rejectFoodOrder(orderId) }
    }

    @Test
    fun `rejectFoodOrder returns failure when api responds with success false`() = runTest {
        val orderId = 202L
        val apiResponse: ApiResponse<String> = ApiResponse(
            success = false,
            message = "Offer expired",
            data = null,
            timestamp = null
        )
        coEvery { driverApi.rejectFoodOrder(orderId) } returns apiResponse

        val result = repository.rejectFoodOrder(orderId)

        assertTrue(result.isFailure)
        assertEquals("Offer expired", result.exceptionOrNull()?.message)
    }

    @Test
    fun `rejectFoodOrder returns failure when api throws exception`() = runTest {
        val orderId = 202L
        coEvery { driverApi.rejectFoodOrder(orderId) } throws RuntimeException("Network timeout")

        val result = repository.rejectFoodOrder(orderId)

        assertTrue(result.isFailure)
        assertEquals("Network timeout", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getDriverFoodOrderDetail returns success when api responds successfully`() = runTest {
        val orderId = 301L
        val dto = com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse(
            id = orderId,
            restaurantName = "Bun Cha Ha Noi",
            restaurantAddress = "12 Hang Buom",
            restaurantLatitude = 21.03,
            restaurantLongitude = 105.85,
            dropOffAddress = "45 Pho Hue",
            dropOffLatitude = 21.01,
            dropOffLongitude = 105.84,
            status = "DELIVERING",
            totalPrice = 85000.0,
            deliveryFee = 15000.0,
            isPaid = true
        )
        val apiResponse = ApiResponse(
            success = true,
            message = "OK",
            data = dto,
            timestamp = null
        )
        coEvery { driverApi.getFoodOrderDetail(orderId) } returns apiResponse

        val result = repository.getDriverFoodOrderDetail(orderId)

        assertTrue(result.isSuccess)
        val order = result.getOrNull()!!
        assertEquals(orderId, order.orderId)
        assertEquals("Bun Cha Ha Noi", order.restaurantName)
        assertEquals(true, order.isPaid)
        assertEquals(0.0, order.cashAmountToCollect, 0.001)
        coVerify(exactly = 1) { driverApi.getFoodOrderDetail(orderId) }
    }

    @Test
    fun `getDriverFoodOrderDetail returns failure when api returns failure response`() = runTest {
        val orderId = 301L
        val apiResponse: ApiResponse<com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse> = ApiResponse(
            success = false,
            message = "Order not found",
            data = null,
            timestamp = null
        )
        coEvery { driverApi.getFoodOrderDetail(orderId) } returns apiResponse

        val result = repository.getDriverFoodOrderDetail(orderId)

        assertTrue(result.isFailure)
        assertEquals("Order not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getDriverActiveFoodOrders filters out completed and cancelled orders`() = runTest {
        val activeOrderDto = com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse(
            id = 401L,
            restaurantName = "Com Tam",
            status = "DELIVERING",
            totalPrice = 50000.0
        )
        val completedOrderDto = com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse(
            id = 402L,
            restaurantName = "Com Tam 2",
            status = "COMPLETED",
            totalPrice = 60000.0
        )
        val cancelledOrderDto = com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse(
            id = 403L,
            restaurantName = "Com Tam 3",
            status = "CANCELLED",
            totalPrice = 70000.0
        )
        val apiResponse = ApiResponse(
            success = true,
            message = "OK",
            data = listOf(activeOrderDto, completedOrderDto, cancelledOrderDto),
            timestamp = null
        )
        coEvery { driverApi.getDriverFoodOrders() } returns apiResponse

        val result = repository.getDriverActiveFoodOrders()

        assertTrue(result.isSuccess)
        val activeList = result.getOrNull()!!
        assertEquals(1, activeList.size)
        assertEquals(401L, activeList[0].orderId)
    }

    @Test
    fun `updateDriverOrderStatus returns success when api call succeeds`() = runTest {
        val orderId = 501L
        val dto = com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse(
            id = orderId,
            restaurantName = "Pho Thin",
            status = "DELIVERING",
            totalPrice = 90000.0
        )
        val apiResponse = ApiResponse(
            success = true,
            message = "OK",
            data = dto,
            timestamp = null
        )
        val expectedRequest = com.example.omnigo.features.driver.food.data.remote.dto.UpdateDriverOrderStatusRequest(
            status = "DELIVERING"
        )
        coEvery { driverApi.updateDriverOrderStatus(orderId, expectedRequest) } returns apiResponse

        val result = repository.updateDriverOrderStatus(orderId, "DELIVERING")

        assertTrue(result.isSuccess)
        assertEquals(com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus.DELIVERING, result.getOrNull()?.status)
        coVerify(exactly = 1) { driverApi.updateDriverOrderStatus(orderId, expectedRequest) }
    }

    @Test
    fun `getDriverAllFoodOrders returns all orders including completed and cancelled`() = runTest {
        val activeOrderDto = com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse(
            id = 401L,
            restaurantName = "Com Tam",
            status = "DELIVERING",
            totalPrice = 50000.0
        )
        val completedOrderDto = com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse(
            id = 402L,
            restaurantName = "Com Tam 2",
            status = "COMPLETED",
            totalPrice = 60000.0
        )
        val cancelledOrderDto = com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse(
            id = 403L,
            restaurantName = "Com Tam 3",
            status = "CANCELLED",
            totalPrice = 70000.0
        )
        val apiResponse = ApiResponse(
            success = true,
            message = "OK",
            data = listOf(activeOrderDto, completedOrderDto, cancelledOrderDto),
            timestamp = null
        )
        coEvery { driverApi.getDriverFoodOrders() } returns apiResponse

        val result = repository.getDriverAllFoodOrders()

        assertTrue(result.isSuccess)
        val allList = result.getOrNull()!!
        assertEquals(3, allList.size)
    }
}
