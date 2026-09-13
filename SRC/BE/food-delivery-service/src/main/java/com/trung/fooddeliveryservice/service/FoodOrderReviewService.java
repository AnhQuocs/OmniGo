package com.trung.fooddeliveryservice.service;

import com.trung.fooddeliveryservice.dto.request.CreateFoodReviewRequest;
import com.trung.fooddeliveryservice.dto.request.ReplyFoodReviewRequest;
import com.trung.fooddeliveryservice.dto.request.UpdateFoodReviewRequest;
import com.trung.fooddeliveryservice.dto.response.FoodReviewResponse;
import com.trung.fooddeliveryservice.dto.response.RestaurantReviewSummaryResponse;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceConflictException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import org.springframework.data.domain.Pageable;

public interface FoodOrderReviewService {

    FoodReviewResponse createReview(Long customerId, CreateFoodReviewRequest request)
            throws ResourceNotFoundException, UnauthorizedException, BadRequestException, ResourceConflictException;

    FoodReviewResponse updateReview(Long customerId, Long reviewId, UpdateFoodReviewRequest request)
            throws ResourceNotFoundException, UnauthorizedException, BadRequestException;

    FoodReviewResponse replyReview(Long merchantOwnerId, Long reviewId, ReplyFoodReviewRequest request)
            throws ResourceNotFoundException, UnauthorizedException;

    FoodReviewResponse getReviewByOrderId(Long orderId)
            throws ResourceNotFoundException;

    FoodReviewResponse getReviewByOrderId(Long orderId, Long requestingUserId)
            throws ResourceNotFoundException;

    RestaurantReviewSummaryResponse getRestaurantReviews(Long restaurantId, Integer rating, Pageable pageable);
}
