package com.example.omnigo.features.customer.food.presentation.viewmodel.review

import androidx.lifecycle.SavedStateHandle
import com.example.omnigo.features.customer.food.domain.model.CreateFoodReviewCommand
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.usecase.GetFoodOrderDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetFoodReviewByOrderUseCase
import com.example.omnigo.features.customer.food.domain.usecase.SubmitFoodReviewUseCase
import com.example.omnigo.features.customer.food.domain.usecase.UploadReviewImageUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class FoodReviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getFoodOrderDetailUseCase: GetFoodOrderDetailUseCase
    private lateinit var getFoodReviewByOrderUseCase: GetFoodReviewByOrderUseCase
    private lateinit var uploadReviewImageUseCase: UploadReviewImageUseCase
    private lateinit var submitFoodReviewUseCase: SubmitFoodReviewUseCase

    private val mockOrder = FoodOrder(
        id = 100L,
        customerId = 1L,
        driverId = 22L,
        driverName = "Tài xế Hoàng",
        driverPhone = "0987654321",
        driverVehiclePlate = "29B1-12345",
        restaurantId = 3L,
        restaurantName = "Cơm Tấm Sài Gòn",
        restaurantAddress = "Hà Nội",
        status = "DELIVERED",
        paymentMethod = PaymentMethod.WALLET,
        isPaid = true,
        itemsPrice = 50000.0,
        deliveryFee = 15000.0,
        totalPrice = 65000.0,
        dropOffAddress = "Ba Đình",
        dropOffLatitude = 21.0333,
        dropOffLongitude = 105.8167,
        note = "",
        items = emptyList(),
        estimatedDeliveryMinutes = 20,
        createdAt = Instant.now().minus(1, ChronoUnit.DAYS).toString()
    )

    @Before
    fun setUp() {
        getFoodOrderDetailUseCase = mockk()
        getFoodReviewByOrderUseCase = mockk()
        uploadReviewImageUseCase = mockk()
        submitFoodReviewUseCase = mockk()

        coEvery { getFoodOrderDetailUseCase(100L) } returns GetFoodOrderDetailResult.Success(mockOrder)
        coEvery { getFoodReviewByOrderUseCase(100L) } returns Result.success(null)
    }

    private fun createViewModel(orderId: Long = 100L): FoodReviewViewModel {
        return FoodReviewViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getFoodReviewByOrderUseCase = getFoodReviewByOrderUseCase,
            uploadReviewImageUseCase = uploadReviewImageUseCase,
            submitFoodReviewUseCase = submitFoodReviewUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to orderId))
        )
    }

    @Test
    fun `init with unreviewed recent order prepares blank review form`() = runTest {
        val vm = createViewModel(100L)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isReadOnly)
        assertFalse(state.isExpired)
        assertEquals("Cơm Tấm Sài Gòn", state.restaurantName)
        assertEquals("Tài xế Hoàng", state.driverName)
        assertEquals(5, state.restaurantRating)
        assertEquals(5, state.driverRating)
        assertTrue(state.canSubmit)
    }

    @Test
    fun `init loads existing review and sets readOnly when review is readOnly`() = runTest {
        val existingReview = FoodReview(
            id = 1L,
            orderId = 100L,
            restaurantRating = 4,
            restaurantComment = "Cơm rất ngon",
            driverRating = 5,
            driverTags = listOf("Thân thiện"),
            reviewPhotos = listOf("https://photo.url"),
            merchantReply = "Cảm ơn bạn",
            merchantReplyAt = null,
            createdAt = "2026-10-20T10:00:00Z",
            isReadOnly = true
        )
        coEvery { getFoodReviewByOrderUseCase(100L) } returns Result.success(existingReview)

        val vm = createViewModel(100L)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isReadOnly)
        assertEquals(4, state.restaurantRating)
        assertEquals("Cơm rất ngon", state.restaurantComment)
        assertEquals(listOf("https://photo.url"), state.uploadedImageUrls)
        assertEquals(setOf("Thân thiện"), state.selectedDriverTags)
        assertFalse(state.canSubmit)
    }

    @Test
    fun `init marks review as expired when order is older than 7 days`() = runTest {
        val oldOrder = mockOrder.copy(
            createdAt = Instant.now().minus(8, ChronoUnit.DAYS).toString()
        )
        coEvery { getFoodOrderDetailUseCase(100L) } returns GetFoodOrderDetailResult.Success(oldOrder)

        val vm = createViewModel(100L)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isExpired)
        assertTrue(state.isReadOnly)
        assertFalse(state.canSubmit)
    }

    @Test
    fun `rating change updates state and toggling driver tag works`() = runTest {
        val vm = createViewModel(100L)
        advanceUntilIdle()

        vm.onRestaurantRatingChanged(3)
        vm.onDriverRatingChanged(4)
        vm.onToggleDriverTag("Giao hàng nhanh")

        val state = vm.uiState.value
        assertEquals(3, state.restaurantRating)
        assertEquals(4, state.driverRating)
        assertTrue(state.selectedDriverTags.contains("Giao hàng nhanh"))

        // Toggle tag off
        vm.onToggleDriverTag("Giao hàng nhanh")
        assertFalse(vm.uiState.value.selectedDriverTags.contains("Giao hàng nhanh"))
    }

    @Test
    fun `submit review invokes usecase and marks success`() = runTest {
        val vm = createViewModel(100L)
        advanceUntilIdle()

        vm.onRestaurantRatingChanged(5)
        vm.onRestaurantCommentChanged("Tuyệt vời")
        vm.onDriverRatingChanged(5)

        val savedReview = FoodReview(
            id = 88L,
            orderId = 100L,
            restaurantRating = 5,
            restaurantComment = "Tuyệt vời",
            driverRating = 5,
            driverTags = emptyList(),
            reviewPhotos = emptyList(),
            merchantReply = null,
            merchantReplyAt = null,
            createdAt = "2026-10-23T09:00:00Z",
            isReadOnly = false
        )

        coEvery { submitFoodReviewUseCase(any()) } returns Result.success(savedReview)

        vm.onSubmitReview()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isSubmitting)
        assertTrue(state.isSubmittedSuccess)
        assertNotNull(state.successMessage)
        assertEquals(savedReview, state.existingReview)
        coVerify(exactly = 1) { submitFoodReviewUseCase(any()) }
    }

    @Test
    fun `double tap submit does not call usecase multiple times`() = runTest {
        val vm = createViewModel(100L)
        advanceUntilIdle()

        coEvery { submitFoodReviewUseCase(any()) } returns Result.success(mockk(relaxed = true))

        // Trigger two rapid submits
        vm.onSubmitReview()
        vm.onSubmitReview()
        advanceUntilIdle()

        coVerify(atMost = 1) { submitFoodReviewUseCase(any()) }
    }
}
