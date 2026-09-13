package com.trung.fooddeliveryservice.controller;

import com.trung.fooddeliveryservice.dto.request.CreateFoodReviewRequest;
import com.trung.fooddeliveryservice.dto.request.ReplyFoodReviewRequest;
import com.trung.fooddeliveryservice.dto.request.UpdateFoodReviewRequest;
import com.trung.fooddeliveryservice.dto.response.ApiResponse;
import com.trung.fooddeliveryservice.dto.response.FoodReviewResponse;
import com.trung.fooddeliveryservice.dto.response.RestaurantReviewSummaryResponse;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceConflictException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.service.CloudinaryService;
import com.trung.fooddeliveryservice.service.FoodOrderReviewService;
import com.trung.fooddeliveryservice.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FoodOrderReviewController {

    private final FoodOrderReviewService reviewService;
    private final CloudinaryService cloudinaryService;

    @PostMapping("/food-reviews")
    public ResponseEntity<ApiResponse<FoodReviewResponse>> createReview(
            @Valid @RequestBody CreateFoodReviewRequest request)
            throws UnauthorizedException, ResourceNotFoundException,
            BadRequestException,
            ResourceConflictException {
        Long customerId = SecurityUtils.getCurrentUserId();
        FoodReviewResponse response = reviewService.createReview(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Đánh giá đơn hàng thành công"));
    }

    @PutMapping("/food-reviews/{id}")
    public ResponseEntity<ApiResponse<FoodReviewResponse>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFoodReviewRequest request)
            throws UnauthorizedException, ResourceNotFoundException,
            BadRequestException {
        Long customerId = SecurityUtils.getCurrentUserId();
        FoodReviewResponse response = reviewService.updateReview(customerId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật đánh giá thành công"));
    }

    @GetMapping("/food-reviews/order/{orderId}")
    public ResponseEntity<ApiResponse<FoodReviewResponse>> getReviewByOrderId(
            @PathVariable Long orderId) throws ResourceNotFoundException {
        Long currentUserId = null;
        try {
            currentUserId = SecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {
        }
        FoodReviewResponse response = reviewService.getReviewByOrderId(orderId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin đánh giá thành công"));
    }

    @GetMapping("/restaurants/{restaurantId}/reviews")
    public ResponseEntity<ApiResponse<RestaurantReviewSummaryResponse>> getRestaurantReviews(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        RestaurantReviewSummaryResponse response = reviewService.getRestaurantReviews(restaurantId, rating, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách đánh giá quán thành công"));
    }

    @PostMapping("/food-reviews/{id}/reply")
    public ResponseEntity<ApiResponse<FoodReviewResponse>> replyReview(
            @PathVariable Long id,
            @Valid @RequestBody ReplyFoodReviewRequest request)
            throws UnauthorizedException, ResourceNotFoundException {
        Long merchantOwnerId = SecurityUtils.getCurrentUserId();
        FoodReviewResponse response = reviewService.replyReview(merchantOwnerId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Gửi phản hồi đánh giá thành công"));
    }

    @PostMapping(value = "/food-reviews/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadReviewImage(
            @RequestParam("file") MultipartFile file) throws BadRequestException {
        String url = cloudinaryService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url != null ? url : ""), "Tải ảnh lên thành công"));
    }
}
