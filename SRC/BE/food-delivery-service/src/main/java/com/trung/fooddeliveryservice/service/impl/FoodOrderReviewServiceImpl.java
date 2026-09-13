package com.trung.fooddeliveryservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trung.fooddeliveryservice.dto.request.CreateFoodReviewRequest;
import com.trung.fooddeliveryservice.dto.request.ReplyFoodReviewRequest;
import com.trung.fooddeliveryservice.dto.request.UpdateFoodReviewRequest;
import com.trung.fooddeliveryservice.dto.response.FoodReviewResponse;
import com.trung.fooddeliveryservice.dto.response.RestaurantReviewSummaryResponse;
import com.trung.fooddeliveryservice.entity.FoodOrder;
import com.trung.fooddeliveryservice.entity.FoodOrderReview;
import com.trung.fooddeliveryservice.entity.Restaurant;
import com.trung.fooddeliveryservice.event.FoodDriverRatedEvent;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceConflictException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.publisher.FoodEventPublisher;
import com.trung.fooddeliveryservice.repository.FoodOrderRepository;
import com.trung.fooddeliveryservice.repository.FoodOrderReviewRepository;
import com.trung.fooddeliveryservice.repository.RestaurantRepository;
import com.trung.fooddeliveryservice.service.FoodOrderReviewService;
import com.trung.fooddeliveryservice.util.enums.OrderStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import com.trung.fooddeliveryservice.service.CloudinaryService;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodOrderReviewServiceImpl implements FoodOrderReviewService {

    private final FoodOrderReviewRepository reviewRepository;
    private final FoodOrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodEventPublisher eventPublisher;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public FoodReviewResponse createReview(Long customerId, CreateFoodReviewRequest request)
            throws ResourceNotFoundException, UnauthorizedException, BadRequestException, ResourceConflictException {
        FoodOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng #" + request.getOrderId()));

        if (!order.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("Bạn không có quyền đánh giá đơn hàng của người khác");
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException("Chỉ có thể đánh giá khi đơn hàng đã hoàn tất (COMPLETED)");
        }

        LocalDateTime completedAt = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
        if (completedAt != null && LocalDateTime.now().isAfter(completedAt.plusDays(7))) {
            throw new BadRequestException("Đơn hàng đã hoàn thành quá 1 tuần (7 ngày), không thể gửi đánh giá mới");
        }

        if (reviewRepository.existsByOrderId(request.getOrderId())) {
            throw new ResourceConflictException("Đơn hàng #" + request.getOrderId() + " đã được đánh giá trước đó");
        }

        if (request.getRestaurantImages() != null && request.getRestaurantImages().size() > 5) {
            throw new BadRequestException("Chỉ được tải lên tối đa 5 ảnh cho mỗi đánh giá");
        }

        Restaurant restaurant = order.getRestaurant();
        LocalDateTime now = LocalDateTime.now();

        FoodOrderReview review = FoodOrderReview.builder()
                .orderId(order.getId())
                .restaurantId(restaurant.getId())
                .customerId(customerId)
                .customerName(order.getCustomerName())
                .driverId(order.getDriverId())
                .restaurantRating(request.getRestaurantRating())
                .restaurantComment(request.getRestaurantComment())
                .restaurantImages(serializeImages(request.getRestaurantImages()))
                .driverRating(request.getDriverRating())
                .driverComment(request.getDriverComment())
                .editableUntil(now.plusHours(48))
                .isEdited(false)
                .build();

        FoodOrderReview savedReview = reviewRepository.save(review);

        // Cập nhật rating và review_count cho quán
        updateRestaurantRatingStats(restaurant.getId());

        // Bắn sự kiện Kafka cập nhật điểm tài xế nếu có
        if (order.getDriverId() != null && request.getDriverRating() != null) {
            FoodDriverRatedEvent driverEvent = FoodDriverRatedEvent.builder()
                    .orderId(order.getId())
                    .driverId(order.getDriverId())
                    .customerId(customerId)
                    .rating(request.getDriverRating())
                    .comment(request.getDriverComment())
                    .build();
            eventPublisher.publishDriverRatedDirect(driverEvent);
        }

        return toResponse(savedReview, customerId);
    }

    @Override
    @Transactional
    public FoodReviewResponse updateReview(Long customerId, Long reviewId, UpdateFoodReviewRequest request)
            throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        FoodOrderReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá #" + reviewId));

        if (!review.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("Bạn không có quyền chỉnh sửa đánh giá này");
        }

        LocalDateTime effectiveLimit = getEffectiveLimit(review);
        if (LocalDateTime.now().isAfter(effectiveLimit)) {
            throw new BadRequestException("Đã quá thời hạn 48 giờ kể từ khi tạo để chỉnh sửa đánh giá này");
        }

        if (request.getRestaurantImages() != null && request.getRestaurantImages().size() > 5) {
            throw new BadRequestException("Chỉ được tải lên tối đa 5 ảnh cho mỗi đánh giá");
        }

        review.setRestaurantRating(request.getRestaurantRating());
        review.setRestaurantComment(request.getRestaurantComment());
        review.setRestaurantImages(serializeImages(request.getRestaurantImages()));
        if (request.getDriverRating() != null) {
            review.setDriverRating(request.getDriverRating());
            review.setDriverComment(request.getDriverComment());
        }
        review.setIsEdited(true);

        FoodOrderReview updated = reviewRepository.save(review);
        updateRestaurantRatingStats(review.getRestaurantId());

        return toResponse(updated, customerId);
    }

    @Override
    @Transactional
    public FoodReviewResponse replyReview(Long merchantOwnerId, Long reviewId, ReplyFoodReviewRequest request)
            throws ResourceNotFoundException, UnauthorizedException {
        FoodOrderReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá #" + reviewId));

        Restaurant restaurant = restaurantRepository.findById(review.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quán ăn #" + review.getRestaurantId()));

        if (!restaurant.getOwnerId().equals(merchantOwnerId)) {
            throw new UnauthorizedException("Chỉ chủ quán mới có quyền trả lời đánh giá này");
        }

        review.setMerchantReply(request.getReply());
        review.setMerchantRepliedAt(LocalDateTime.now());

        FoodOrderReview updated = reviewRepository.save(review);
        return toResponse(updated, null);
    }

    @Override
    @Transactional(readOnly = true)
    public FoodReviewResponse getReviewByOrderId(Long orderId)
            throws ResourceNotFoundException {
        return getReviewByOrderId(orderId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public FoodReviewResponse getReviewByOrderId(Long orderId, Long requestingUserId)
            throws ResourceNotFoundException {
        FoodOrderReview review = reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa có đánh giá cho đơn hàng #" + orderId));
        return toResponse(review, requestingUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantReviewSummaryResponse getRestaurantReviews(Long restaurantId, Integer rating, Pageable pageable) {
        Page<FoodOrderReview> page;
        if (rating != null && rating >= 1 && rating <= 5) {
            page = reviewRepository.findByRestaurantIdAndRestaurantRatingOrderByCreatedAtDesc(restaurantId, rating, pageable);
        } else {
            page = reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable);
        }

        Double avgRating = reviewRepository.calculateAverageRating(restaurantId);
        long total = reviewRepository.countByRestaurantId(restaurantId);

        Map<Integer, Long> counts = new HashMap<>();
        for (int r = 1; r <= 5; r++) {
            counts.put(r, reviewRepository.countByRestaurantIdAndRestaurantRating(restaurantId, r));
        }

        return RestaurantReviewSummaryResponse.builder()
                .restaurantId(restaurantId)
                .averageRating(roundOneDecimal(avgRating))
                .totalReviews(total)
                .ratingCounts(counts)
                .reviews(page.map(r -> toResponse(r, null)))
                .build();
    }

    private void updateRestaurantRatingStats(Long restaurantId) {
        restaurantRepository.findById(restaurantId).ifPresent(restaurant -> {
            Double avg = reviewRepository.calculateAverageRating(restaurantId);
            long count = reviewRepository.countByRestaurantId(restaurantId);
            restaurant.setRating(roundOneDecimal(avg));
            restaurant.setReviewCount((int) count);
            restaurantRepository.save(restaurant);
        });
    }

    private Double roundOneDecimal(Double val) {
        if (val == null) return 5.0;
        return Math.round(val * 10.0) / 10.0;
    }

    private String serializeImages(List<String> images) {
        if (images == null || images.isEmpty()) return null;
        List<String> processedUrls = images.stream()
                .filter(Objects::nonNull)
                .map(img -> cloudinaryService != null ? cloudinaryService.uploadBase64(img) : img)
                .filter(Objects::nonNull)
                .toList();
        try {
            return objectMapper.writeValueAsString(processedUrls);
        } catch (Exception e) {
            return String.join(",", processedUrls);
        }
    }

    private List<String> deserializeImages(String raw) {
        if (raw == null || raw.trim().isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Arrays.asList(raw.split(","));
        }
    }

    private LocalDateTime getEffectiveLimit(FoodOrderReview r) {
        if (r.getCreatedAt() != null) {
            return r.getCreatedAt().plusHours(48);
        }
        if (r.getEditableUntil() != null) {
            return r.getEditableUntil();
        }
        return LocalDateTime.now().plusHours(48);
    }

    private FoodReviewResponse toResponse(FoodOrderReview r, Long requestingUserId) {
        LocalDateTime effectiveLimit = getEffectiveLimit(r);
        boolean isWithin48h = LocalDateTime.now().isBefore(effectiveLimit);
        boolean isAuthorOrUnspecified = requestingUserId == null || requestingUserId.equals(r.getCustomerId());
        boolean canEdit = isWithin48h && isAuthorOrUnspecified;

        return FoodReviewResponse.builder()
                .id(r.getId())
                .orderId(r.getOrderId())
                .restaurantId(r.getRestaurantId())
                .customerId(r.getCustomerId())
                .customerName(r.getCustomerName())
                .driverId(r.getDriverId())
                .restaurantRating(r.getRestaurantRating())
                .restaurantComment(r.getRestaurantComment())
                .restaurantImages(deserializeImages(r.getRestaurantImages()))
                .driverRating(r.getDriverRating())
                .driverComment(r.getDriverComment())
                .merchantReply(r.getMerchantReply())
                .merchantRepliedAt(r.getMerchantRepliedAt())
                .editableUntil(effectiveLimit)
                .isEdited(r.getIsEdited())
                .canEdit(canEdit)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
