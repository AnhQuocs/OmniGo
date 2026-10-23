package com.example.omnigo.features.customer.food.domain.usecase

import com.example.omnigo.features.customer.food.domain.model.CreateFoodReviewCommand
import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.features.customer.food.domain.repository.FoodReviewRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubmitFoodReviewUseCaseTest {

    private lateinit var repository: FoodReviewRepository
    private lateinit var useCase: SubmitFoodReviewUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SubmitFoodReviewUseCase(repository)
    }

    @Test
    fun `invoke with invalid orderId returns failure`() = runTest {
        val command = CreateFoodReviewCommand(
            orderId = 0L,
            restaurantRating = 5,
            driverRating = 5
        )

        val result = useCase(command)

        assertTrue(result.isFailure)
        assertEquals("Invalid orderId", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with restaurantRating outside range returns failure`() = runTest {
        val commandZero = CreateFoodReviewCommand(orderId = 10L, restaurantRating = 0, driverRating = 5)
        val commandSix = CreateFoodReviewCommand(orderId = 10L, restaurantRating = 6, driverRating = 5)

        assertTrue(useCase(commandZero).isFailure)
        assertTrue(useCase(commandSix).isFailure)
    }

    @Test
    fun `invoke with driverRating outside range returns failure`() = runTest {
        val commandZero = CreateFoodReviewCommand(orderId = 10L, restaurantRating = 5, driverRating = 0)
        val commandSix = CreateFoodReviewCommand(orderId = 10L, restaurantRating = 5, driverRating = 6)

        assertTrue(useCase(commandZero).isFailure)
        assertTrue(useCase(commandSix).isFailure)
    }

    @Test
    fun `invoke with more than 5 photos returns failure`() = runTest {
        val command = CreateFoodReviewCommand(
            orderId = 10L,
            restaurantRating = 5,
            driverRating = 5,
            reviewPhotos = listOf("1", "2", "3", "4", "5", "6")
        )

        val result = useCase(command)

        assertTrue(result.isFailure)
        assertEquals("Maximum 5 review photos allowed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with valid command delegates to repository and returns success`() = runTest {
        val command = CreateFoodReviewCommand(
            orderId = 10L,
            restaurantRating = 5,
            driverRating = 5,
            reviewPhotos = listOf("url1", "url2")
        )

        val expectedReview = FoodReview(
            id = 1L,
            orderId = 10L,
            restaurantRating = 5,
            restaurantComment = null,
            driverRating = 5,
            driverTags = emptyList(),
            reviewPhotos = listOf("url1", "url2"),
            merchantReply = null,
            merchantReplyAt = null,
            createdAt = "2026-10-23T09:00:00Z",
            isReadOnly = false
        )

        coEvery { repository.submitFoodReview(command) } returns Result.success(expectedReview)

        val result = useCase(command)

        assertTrue(result.isSuccess)
        assertEquals(expectedReview, result.getOrNull())
    }
}
