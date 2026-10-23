package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.CreateFoodReviewRequest
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderReviewResponse
import com.example.omnigo.features.customer.food.domain.model.CreateFoodReviewCommand
import com.example.omnigo.features.customer.food.domain.model.FoodReview

fun FoodOrderReviewResponse.toDomain(): FoodReview {
    return FoodReview(
        id = id ?: 0L,
        orderId = orderId ?: 0L,
        restaurantRating = restaurantRating ?: 5,
        restaurantComment = restaurantComment,
        driverRating = driverRating ?: 5,
        driverTags = driverTags ?: emptyList(),
        reviewPhotos = reviewPhotos ?: emptyList(),
        merchantReply = merchantReply,
        merchantReplyAt = merchantReplyAt,
        createdAt = createdAt,
        isReadOnly = readOnly ?: false
    )
}

fun CreateFoodReviewCommand.toRequest(): CreateFoodReviewRequest {
    return CreateFoodReviewRequest(
        orderId = orderId,
        restaurantRating = restaurantRating,
        restaurantComment = restaurantComment?.takeIf { it.isNotBlank() },
        driverRating = driverRating,
        driverTags = driverTags.takeIf { it.isNotEmpty() },
        reviewPhotos = reviewPhotos.takeIf { it.isNotEmpty() }
    )
}
