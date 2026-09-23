package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.error.FoodError
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantsResult
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

class GetRestaurantsUseCaseTest {

    private lateinit var repository: FoodRepository
    private lateinit var useCase: GetRestaurantsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetRestaurantsUseCase(repository)
    }

    @Test
    fun `invoke should return list of restaurants on success`() = runTest {
        val mockRestaurants = listOf(
            Restaurant(
                id = 1L,
                name = "Bún Bò Huế Mệ Kéo",
                phone = "0901234567",
                address = "20 Bạch Đằng",
                latitude = 16.4637,
                longitude = 107.5909,
                imageUrl = "https://example.com/image.jpg",
                status = "OPEN",
                openTime = "06:00",
                closeTime = "22:00",
                rating = 4.8,
                reviewCount = 126,
                isOpen = true,
                menuItems = emptyList()
            )
        )
        coEvery { repository.getRestaurants(null) } returns GetRestaurantsResult.Success(mockRestaurants)

        val result = useCase(null)

        assertTrue(result is GetRestaurantsResult.Success)
        val successResult = result as GetRestaurantsResult.Success
        assertEquals(1, successResult.restaurants.size)
        assertEquals("Bún Bò Huế Mệ Kéo", successResult.restaurants.first().name)
        coVerify(exactly = 1) { repository.getRestaurants(null) }
    }

    @Test
    fun `invoke with search query should pass query to repository`() = runTest {
        val searchQuery = "Cơm Tấm"
        coEvery { repository.getRestaurants(searchQuery) } returns GetRestaurantsResult.Success(emptyList())

        val result = useCase(searchQuery)

        assertTrue(result is GetRestaurantsResult.Success)
        val successResult = result as GetRestaurantsResult.Success
        assertEquals(0, successResult.restaurants.size)
        coVerify(exactly = 1) { repository.getRestaurants(searchQuery) }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        coEvery { repository.getRestaurants(null) } returns GetRestaurantsResult.Error(FoodError.NETWORK_ERROR)

        val result = useCase(null)

        assertTrue(result is GetRestaurantsResult.Error)
        val errorResult = result as GetRestaurantsResult.Error
        assertEquals(FoodError.NETWORK_ERROR, errorResult.error)
        coVerify(exactly = 1) { repository.getRestaurants(null) }
    }
}
