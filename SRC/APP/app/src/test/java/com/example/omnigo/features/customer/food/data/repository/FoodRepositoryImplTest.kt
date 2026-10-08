package com.example.omnigo.features.customer.food.data.repository

import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.customer.food.data.remote.api.FoodApi
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse
import com.example.omnigo.features.customer.food.data.remote.dto.DriverProfileResponse
import com.example.omnigo.features.customer.food.data.remote.dto.MenuItemResponse
import com.example.omnigo.features.customer.food.data.remote.dto.RestaurantResponse
import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.CreateFoodOrderResult
import com.example.omnigo.features.customer.food.domain.model.FoodOrderCreateCommand
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
import com.example.omnigo.features.customer.food.domain.model.GetDriverProfileResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class FoodRepositoryImplTest {

    private lateinit var foodApi: FoodApi
    private lateinit var repository: FoodRepositoryImpl

    private val mockDto = RestaurantResponse(
        id = 1L,
        ownerId = 10L,
        name = "Bún Chả Hương Liên",
        phone = "0243987654",
        address = "24 Lê Văn Hưu, Hà Nội",
        latitude = 21.0190,
        longitude = 105.8520,
        imageUrl = "https://example.com/buncha.jpg",
        status = "OPEN",
        openTime = "08:00",
        closeTime = "21:00",
        rating = 4.8,
        reviewCount = 500,
        totalReviews = 500,
        isLocked = false,
        lockedReason = null,
        lockedAt = null,
        menuItems = listOf(
            MenuItemResponse(
                id = 201L,
                restaurantId = 1L,
                name = "Bún chả đặc biệt",
                description = "Nướng than hoa",
                price = 60000.0,
                imageUrl = "https://example.com/bc.jpg",
                category = "Bún chả",
                isAvailable = true,
                createdAt = null,
                updatedAt = null
            )
        ),
        createdAt = null,
        updatedAt = null
    )

    @Before
    fun setUp() {
        foodApi = mockk()
        repository = FoodRepositoryImpl(foodApi)
    }

    @Test
    fun `getRestaurants returns Success when api returns data`() = runTest {
        val apiResponse = ApiResponse(
            success = true,
            message = "OK",
            data = listOf(mockDto),
            timestamp = null
        )
        coEvery { foodApi.getRestaurants(any()) } returns apiResponse

        val result = repository.getRestaurants(null)

        assertTrue(result is GetRestaurantsResult.Success)
        val success = result as GetRestaurantsResult.Success
        assertEquals(1, success.restaurants.size)
        assertEquals("Bún Chả Hương Liên", success.restaurants[0].name)
        assertEquals(1, success.restaurants[0].menuItems.size)
    }

    @Test
    fun `getRestaurantDetail returns Success when api returns restaurant`() = runTest {
        val apiResponse = ApiResponse(
            success = true,
            message = "OK",
            data = mockDto,
            timestamp = null
        )
        coEvery { foodApi.getRestaurantDetail(1L) } returns apiResponse

        val result = repository.getRestaurantDetail(1L)

        assertTrue(result is GetRestaurantDetailResult.Success)
        val success = result as GetRestaurantDetailResult.Success
        assertEquals(1L, success.restaurant.id)
        assertEquals("Bún Chả Hương Liên", success.restaurant.name)
        assertEquals(60000.0, success.restaurant.menuItems[0].price, 0.01)
    }

    @Test
    fun `getRestaurantDetail returns NetworkError on IOException`() = runTest {
        coEvery { foodApi.getRestaurantDetail(1L) } throws IOException("No connection")

        val result = repository.getRestaurantDetail(1L)

        assertTrue(result is GetRestaurantDetailResult.Error)
        val error = result as GetRestaurantDetailResult.Error
        assertEquals(FoodError.NETWORK_ERROR, error.error)
    }

    @Test
    fun `getRestaurantDetail returns ServerError when api response is not successful`() = runTest {
        val apiResponse = ApiResponse<RestaurantResponse>(
            success = false,
            message = "Not found",
            data = null,
            timestamp = null
        )
        coEvery { foodApi.getRestaurantDetail(99L) } returns apiResponse

        val result = repository.getRestaurantDetail(99L)

        assertTrue(result is GetRestaurantDetailResult.Error)
        val error = result as GetRestaurantDetailResult.Error
        assertEquals(FoodError.SERVER_ERROR, error.error)
    }

    @Test
    fun `createFoodOrder preserves order id and backend prices in mapped result`() = runTest {
        val command = FoodOrderCreateCommand(
            restaurantId = 1L,
            dropOffAddress = "Delivery address",
            dropOffLatitude = 21.0,
            dropOffLongitude = 105.0,
            items = emptyList(),
            note = null,
            paymentMethod = PaymentMethod.CASH
        )
        coEvery { foodApi.createFoodOrder(any()) } returns ApiResponse(
            success = true,
            message = "Created",
            data = FoodOrderResponse(
                id = 999L,
                status = "PENDING",
                totalPrice = 100_123.0,
                deliveryFee = 12_345.0
            ),
            timestamp = null
        )

        val result = repository.createFoodOrder(command)

        assertTrue(result is CreateFoodOrderResult.Success)
        val order = (result as CreateFoodOrderResult.Success).order
        assertEquals(999L, order.id)
        assertEquals(100_123.0, order.totalPrice!!, 0.001)
        assertEquals(12_345.0, order.deliveryFee!!, 0.001)
        io.mockk.coVerify(exactly = 1) {
            foodApi.createFoodOrder(
                match {
                    it.restaurantId == 1L &&
                        it.dropOffAddress == "Delivery address" &&
                        it.paymentMethod == "CASH"
                }
            )
        }
    }

    @Test
    fun `getDriverProfile maps optional profile fields from internal endpoint`() = runTest {
        coEvery { foodApi.getDriverProfile(77L) } returns ApiResponse(
            success = true,
            message = "OK",
            data = DriverProfileResponse(
                id = 77L,
                driverName = "Driver Name",
                driverPhone = "0900000000",
                vehiclePlate = "ABC-123",
                avatarUrl = null
            ),
            timestamp = null
        )

        val result = repository.getDriverProfile(77L)

        assertTrue(result is GetDriverProfileResult.Success)
        val profile = (result as GetDriverProfileResult.Success).profile
        assertEquals(77L, profile.driverId)
        assertEquals("Driver Name", profile.driverName)
        assertEquals("0900000000", profile.driverPhone)
        assertEquals("ABC-123", profile.vehiclePlate)
        assertEquals(null, profile.avatarUrl)
    }

    @Test
    fun `getDriverProfile reports network error`() = runTest {
        coEvery { foodApi.getDriverProfile(77L) } throws IOException("No connection")

        val result = repository.getDriverProfile(77L)

        assertTrue(result is GetDriverProfileResult.Error)
        assertEquals(FoodError.NETWORK_ERROR, (result as GetDriverProfileResult.Error).error)
    }
}
