package com.trung.bookingservice.controller;

import com.trung.bookingservice.dto.request.CreateBookingReviewRequest;
import com.trung.bookingservice.dto.response.ApiResponse;
import com.trung.bookingservice.dto.response.BookingReviewResponse;
import com.trung.bookingservice.dto.response.DriverBookingReviewSummaryResponse;
import com.trung.bookingservice.exception.BadRequestException;
import com.trung.bookingservice.exception.ResourceConflictException;
import com.trung.bookingservice.exception.ResourceNotFoundException;
import com.trung.bookingservice.service.BookingReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingReviewController {

    private final BookingReviewService reviewService;

    @PostMapping(value = {"/{bookingId}/reviews", "/{bookingId}/review"})
    public ResponseEntity<ApiResponse<BookingReviewResponse>> createReview(
            @RequestHeader("X-User-Id") Long customerId,
            @PathVariable Long bookingId,
            @Valid @RequestBody CreateBookingReviewRequest request)
            throws BadRequestException, ResourceNotFoundException, ResourceConflictException {
        BookingReviewResponse response = reviewService.createReview(customerId, bookingId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BookingReviewResponse>builder()
                        .success(true)
                        .message("Đánh giá chuyến đi thành công")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @GetMapping(value = {"/{bookingId}/reviews", "/{bookingId}/review"})
    public ResponseEntity<ApiResponse<BookingReviewResponse>> getReviewByBookingId(
            @PathVariable Long bookingId) throws ResourceNotFoundException {
        BookingReviewResponse response = reviewService.getReviewByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.<BookingReviewResponse>builder()
                .success(true)
                .message("Lấy thông tin đánh giá chuyến đi thành công")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping(value = {"/driver/{driverId}/reviews", "/driver/reviews"})
    public ResponseEntity<ApiResponse<DriverBookingReviewSummaryResponse>> getDriverReviews(
            @PathVariable(required = false) Long driverId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerDriverId,
            @RequestParam(required = false) Integer rating) {
        Long targetDriverId = driverId != null ? driverId : headerDriverId;
        if (targetDriverId == null) {
            targetDriverId = 1L; // fallback
        }
        DriverBookingReviewSummaryResponse response = reviewService.getDriverReviews(targetDriverId, rating);
        return ResponseEntity.ok(ApiResponse.<DriverBookingReviewSummaryResponse>builder()
                .success(true)
                .message("Lấy danh sách đánh giá tài xế thành công")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
