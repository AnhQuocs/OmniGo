package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetRestaurantDetailUseCaseTest {

    private lateinit var repository: FoodRepository
    private lateinit var useCase: GetRestaurantDetailUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetRestaurantDetailUseCase(repository)
    }

    @Test
    fun `invoke should return restaurant detail on success`() = runTest {
        val mockRestaurant = Restaurant(
            id = 1L,
            name = "Phở Thìn Lò Đúc",
            phone = "0987654321",
            address = "13 Lò Đúc, Hai Bà Trưng, Hà Nội",
            latitude = 21.0180,
            longitude = 105.8560,
            imageUrl = "https://example.com/pho.jpg",
            status = "OPEN",
            openTime = "06:00",
            closeTime = "21:00",
            rating = 4.9,
            reviewCount = 350,
            isOpen = true,
            menuItems = emptyList()
        )
        coEvery { repository.getRestaurantDetail(1L) } returns GetRestaurantDetailResult.Success(mockRestaurant)

        val result = useCase(1L)

        assertTrue(result is GetRestaurantDetailResult.Success)
        val success = result as GetRestaurantDetailResult.Success
        assertEquals(1L, success.restaurant.id)
        assertEquals("Phở Thìn Lò Đúc", success.restaurant.name)
        coVerify(exactly = 1) { repository.getRestaurantDetail(1L) }
    }

    @Test
    fun `invoke should return error when repository returns error`() = runTest {
        coEvery { repository.getRestaurantDetail(999L) } returns GetRestaurantDetailResult.Error(FoodError.SERVER_ERROR)

        val result = useCase(999L)

        assertTrue(result is GetRestaurantDetailResult.Error)
        val error = result as GetRestaurantDetailResult.Error
        assertEquals(FoodError.SERVER_ERROR, error.error)
        coVerify(exactly = 1) { repository.getRestaurantDetail(999L) }
    }
}
