package com.trung.bookingservice.service.impl;

import com.trung.bookingservice.dto.request.CreateBookingReviewRequest;
import com.trung.bookingservice.dto.response.BookingReviewResponse;
import com.trung.bookingservice.dto.response.DriverBookingReviewSummaryResponse;
import com.trung.bookingservice.entity.Booking;
import com.trung.bookingservice.entity.BookingReview;
import com.trung.bookingservice.exception.BadRequestException;
import com.trung.bookingservice.exception.ResourceConflictException;
import com.trung.bookingservice.exception.ResourceNotFoundException;
import com.trung.bookingservice.repository.BookingRepository;
import com.trung.bookingservice.repository.BookingReviewRepository;
import com.trung.bookingservice.service.BookingReviewService;
import com.trung.bookingservice.util.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingReviewServiceImpl implements BookingReviewService {

    private final BookingReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public BookingReviewResponse createReview(Long customerId, Long bookingId, CreateBookingReviewRequest request)
            throws BadRequestException, ResourceNotFoundException, ResourceConflictException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi #" + bookingId));

        if (!booking.getCustomerId().equals(customerId)) {
            throw new BadRequestException("Bạn không có quyền đánh giá chuyến đi của khách hàng khác");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Chỉ có thể đánh giá chuyến đi sau khi đã hoàn thành");
        }

        if (booking.getDriverId() == null) {
            throw new BadRequestException("Chuyến đi chưa có thông tin tài xế để đánh giá");
        }

        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ResourceConflictException("Chuyến đi này đã được đánh giá trước đó");
        }

        BookingReview review = BookingReview.builder()
                .bookingId(bookingId)
                .driverId(booking.getDriverId())
                .customerId(customerId)
                .customerName(booking.getCustomerName() != null ? booking.getCustomerName() : "Khách hàng")
                .customerAvatarUrl(booking.getCustomerAvatarUrl())
                .rating(request.getRating())
                .comment(request.getComment() != null ? request.getComment().trim() : "")
                .build();

        BookingReview saved = reviewRepository.save(review);
        log.info("Khách hàng {} đã đánh giá {} sao cho tài xế {} sau chuyến đi #{}",
                customerId, request.getRating(), booking.getDriverId(), bookingId);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingReviewResponse getReviewByBookingId(Long bookingId)
            throws ResourceNotFoundException {
        BookingReview review = reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa có đánh giá cho chuyến đi #" + bookingId));
        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverBookingReviewSummaryResponse getDriverReviews(Long driverId, Integer rating) {
        List<BookingReview> reviews;
        if (rating != null && rating >= 1 && rating <= 5) {
            reviews = reviewRepository.findByDriverIdAndRatingOrderByCreatedAtDesc(driverId, rating);
        } else {
            reviews = reviewRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        }

        Double avgRating = reviewRepository.calculateAverageDriverRating(driverId);
        long total = reviewRepository.countByDriverId(driverId);

        Map<Integer, Long> counts = new HashMap<>();
        for (int r = 1; r <= 5; r++) {
            counts.put(r, reviewRepository.countByDriverIdAndRating(driverId, r));
        }

        List<BookingReviewResponse> responses = reviews.stream()
                .map(this::toResponse)
                .toList();

        return DriverBookingReviewSummaryResponse.builder()
                .driverId(driverId)
                .averageRating(roundOneDecimal(avgRating))
                .totalReviews(total)
                .ratingCounts(counts)
                .reviews(responses)
                .build();
    }

    private BookingReviewResponse toResponse(BookingReview r) {
        return BookingReviewResponse.builder()
                .id(r.getId())
                .bookingId(r.getBookingId())
                .driverId(r.getDriverId())
                .customerId(r.getCustomerId())
                .customerName(r.getCustomerName())
                .customerAvatarUrl(r.getCustomerAvatarUrl())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private Double roundOneDecimal(Double val) {
        if (val == null) return 5.0;
        return Math.round(val * 10.0) / 10.0;
    }
}
