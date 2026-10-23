package com.example.omnigo.features.customer.food.presentation.viewmodel.review

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.CreateFoodReviewCommand
import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult
import com.example.omnigo.features.customer.food.domain.usecase.GetFoodOrderDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetFoodReviewByOrderUseCase
import com.example.omnigo.features.customer.food.domain.usecase.SubmitFoodReviewUseCase
import com.example.omnigo.features.customer.food.domain.usecase.UploadReviewImageUseCase
import com.example.omnigo.utils.ImageUploadUtils
import com.example.omnigo.utils.UiText
import com.example.omnigo.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class FoodReviewViewModel @Inject constructor(
    private val getFoodOrderDetailUseCase: GetFoodOrderDetailUseCase,
    private val getFoodReviewByOrderUseCase: GetFoodReviewByOrderUseCase,
    private val uploadReviewImageUseCase: UploadReviewImageUseCase,
    private val submitFoodReviewUseCase: SubmitFoodReviewUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: Long = savedStateHandle.get<Long>("orderId") ?: 0L

    private val _uiState = MutableStateFlow(FoodReviewUiState(orderId = orderId))
    val uiState: StateFlow<FoodReviewUiState> = _uiState.asStateFlow()
    private var submitJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    fun loadData() {
        if (orderId <= 0) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = UiText.StringResource(R.string.order_detail_invalid_order_id)
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Fetch Order Details (Restaurant name, driver name, completion time)
            var isExpired = false
            when (val orderResult = getFoodOrderDetailUseCase(orderId)) {
                is GetFoodOrderDetailResult.Success -> {
                    val order = orderResult.order
                    isExpired = checkOrderExpired(order.createdAt)
                    _uiState.update {
                        it.copy(
                            restaurantName = order.restaurantName,
                            driverName = order.driverName,
                            isExpired = isExpired
                        )
                    }
                }
                is GetFoodOrderDetailResult.Error -> {
                    // Non-fatal, continue checking review
                }
            }

            // 2. Fetch existing review for this order
            val reviewResult = getFoodReviewByOrderUseCase(orderId)
            reviewResult.fold(
                onSuccess = { review ->
                    if (review != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                existingReview = review,
                                restaurantRating = review.restaurantRating,
                                restaurantComment = review.restaurantComment ?: "",
                                driverRating = review.driverRating,
                                selectedDriverTags = review.driverTags.toSet(),
                                uploadedImageUrls = review.reviewPhotos,
                                isReadOnly = review.isReadOnly || isExpired
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isReadOnly = isExpired
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message?.let { msg -> UiText.DynamicString(msg) }
                                ?: UiText.StringResource(R.string.food_review_load_failed)
                        )
                    }
                }
            )
        }
    }

    fun onRestaurantRatingChanged(rating: Int) {
        if (_uiState.value.isReadOnly || _uiState.value.isExpired) return
        _uiState.update { it.copy(restaurantRating = rating.coerceIn(1, 5)) }
    }

    fun onRestaurantCommentChanged(comment: String) {
        if (_uiState.value.isReadOnly || _uiState.value.isExpired) return
        _uiState.update { it.copy(restaurantComment = comment) }
    }

    fun onDriverRatingChanged(rating: Int) {
        if (_uiState.value.isReadOnly || _uiState.value.isExpired) return
        _uiState.update { it.copy(driverRating = rating.coerceIn(1, 5)) }
    }

    fun onToggleDriverTag(tag: String) {
        if (_uiState.value.isReadOnly || _uiState.value.isExpired) return
        _uiState.update { current ->
            val updated = current.selectedDriverTags.toMutableSet()
            if (updated.contains(tag)) {
                updated.remove(tag)
            } else {
                updated.add(tag)
            }
            current.copy(selectedDriverTags = updated)
        }
    }

    fun onUploadImage(context: Context, uri: Uri) {
        val currentState = _uiState.value
        if (currentState.isReadOnly || currentState.isExpired || !currentState.canAddMorePhotos) {
            return
        }

        val part = ImageUploadUtils.uriToMultipart(
            context = context,
            uri = uri,
            partName = "file",
            fileName = "review_image_${System.currentTimeMillis()}.jpg"
        )

        if (part == null) {
            _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.food_review_image_invalid))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true, errorMessage = null) }
            val result = uploadReviewImageUseCase(part)
            result.fold(
                onSuccess = { url ->
                    _uiState.update { current ->
                        val updatedList = current.uploadedImageUrls + url
                        current.copy(
                            isUploadingImage = false,
                            uploadedImageUrls = updatedList.take(5)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUploadingImage = false,
                            errorMessage = error.message?.let { msg -> UiText.DynamicString(msg) }
                                ?: UiText.StringResource(R.string.food_review_image_upload_failed)
                        )
                    }
                }
            )
        }
    }

    fun onRemoveImage(index: Int) {
        if (_uiState.value.isReadOnly || _uiState.value.isExpired) return
        _uiState.update { current ->
            if (index in current.uploadedImageUrls.indices) {
                val updated = current.uploadedImageUrls.toMutableList()
                updated.removeAt(index)
                current.copy(uploadedImageUrls = updated)
            } else {
                current
            }
        }
    }

    fun onSubmitReview() {
        if (submitJob?.isActive == true) return
        val currentState = _uiState.value
        if (!currentState.canSubmit) return

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        submitJob = viewModelScope.launch {
            val command = CreateFoodReviewCommand(
                orderId = orderId,
                restaurantRating = currentState.restaurantRating,
                restaurantComment = currentState.restaurantComment,
                driverRating = currentState.driverRating,
                driverTags = currentState.selectedDriverTags.toList(),
                reviewPhotos = currentState.uploadedImageUrls
            )

            val result = submitFoodReviewUseCase(command)
            result.fold(
                onSuccess = { review ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSubmittedSuccess = true,
                            existingReview = review,
                            successMessage = UiText.StringResource(R.string.food_review_submit_success)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message?.let { msg -> UiText.DynamicString(msg) }
                                ?: UiText.StringResource(R.string.food_review_submit_failed)
                        )
                    }
                }
            )
        }
    }

    fun onClearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onClearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun checkOrderExpired(createdAt: String?): Boolean {
        if (createdAt.isNullOrBlank()) return false
        return try {
            val orderTime = Instant.parse(createdAt)
            val now = Instant.now()
            val daysBetween = ChronoUnit.DAYS.between(orderTime, now)
            daysBetween > 7
        } catch (_: Exception) {
            false
        }
    }
}
