package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.CreateFoodReviewRequest
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderReviewResponse
import com.example.omnigo.features.customer.food.domain.model.CreateFoodReviewCommand
import com.example.omnigo.features.customer.food.domain.model.FoodReview

fun FoodOrderReviewResponse.toDomain(): FoodReview {
    val parsedTags = when {
        !driverTags.isNullOrEmpty() -> driverTags
        !driverComment.isNullOrBlank() -> driverComment.split(",").map { it.trim() }.filter { it.isNotBlank() }
        else -> emptyList()
    }
    val editable = canEdit ?: (updatedAt == null && !(readOnly ?: false))
    return FoodReview(
        id = id ?: 0L,
        orderId = orderId ?: 0L,
        restaurantRating = restaurantRating ?: 5,
        restaurantComment = restaurantComment,
        driverRating = driverRating ?: 5,
        driverTags = parsedTags,
        reviewPhotos = restaurantImages ?: emptyList(),
        merchantReply = merchantReply,
        merchantReplyAt = merchantReplyAt,
        createdAt = createdAt,
        isReadOnly = readOnly ?: true,
        customerId = customerId ?: 0L,
        customerName = customerName,
        driverComment = driverComment ?: parsedTags.joinToString(", ").takeIf { it.isNotBlank() },
        updatedAt = updatedAt,
        canEdit = editable
    )
}

fun CreateFoodReviewCommand.toRequest(): CreateFoodReviewRequest {
    return CreateFoodReviewRequest(
        orderId = orderId,
        restaurantRating = restaurantRating,
        restaurantComment = restaurantComment?.takeIf { it.isNotBlank() },
        driverRating = driverRating,
        driverComment = driverTags.joinToString(", ").takeIf { it.isNotBlank() },
        images = reviewPhotos.takeIf { it.isNotEmpty() }
    )
}
