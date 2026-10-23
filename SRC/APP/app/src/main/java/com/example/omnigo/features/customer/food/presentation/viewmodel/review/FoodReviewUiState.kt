package com.example.omnigo.features.customer.food.presentation.viewmodel.review

import com.example.omnigo.features.customer.food.domain.model.FoodReview
import com.example.omnigo.utils.UiText

data class FoodReviewUiState(
    val isLoading: Boolean = true,
    val orderId: Long = 0L,
    val restaurantName: String? = null,
    val driverName: String? = null,
    val restaurantRating: Int = 5,
    val restaurantComment: String = "",
    val driverRating: Int = 5,
    val selectedDriverTags: Set<String> = emptySet(),
    val availableDriverTags: List<String> = listOf(
        "Giao hàng nhanh",
        "Thân thiện",
        "Bảo quản cẩn thận",
        "Đúng giờ",
        "Nhiệt tình"
    ),
    val uploadedImageUrls: List<String> = emptyList(),
    val isUploadingImage: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmittedSuccess: Boolean = false,
    val isReadOnly: Boolean = false,
    val isExpired: Boolean = false,
    val existingReview: FoodReview? = null,
    val errorMessage: UiText? = null,
    val successMessage: UiText? = null
) {
    val canAddMorePhotos: Boolean
        get() = uploadedImageUrls.size < 5 && !isReadOnly && !isExpired

    val canSubmit: Boolean
        get() = !isLoading &&
            !isSubmitting &&
            !isSubmittedSuccess &&
            !isUploadingImage &&
            !isReadOnly &&
            !isExpired &&
            restaurantRating in 1..5 &&
            driverRating in 1..5
}
