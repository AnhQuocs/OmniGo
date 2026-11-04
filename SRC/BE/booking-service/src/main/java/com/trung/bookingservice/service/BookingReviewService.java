package com.trung.bookingservice.service;

import com.trung.bookingservice.dto.request.CreateBookingReviewRequest;
import com.trung.bookingservice.dto.response.BookingReviewResponse;
import com.trung.bookingservice.dto.response.DriverBookingReviewSummaryResponse;
import com.trung.bookingservice.exception.BadRequestException;
import com.trung.bookingservice.exception.ResourceConflictException;
import com.trung.bookingservice.exception.ResourceNotFoundException;

public interface BookingReviewService {

    BookingReviewResponse createReview(Long customerId, Long bookingId, CreateBookingReviewRequest request)
            throws BadRequestException, ResourceNotFoundException, ResourceConflictException;

    BookingReviewResponse getReviewByBookingId(Long bookingId)
            throws ResourceNotFoundException;

    DriverBookingReviewSummaryResponse getDriverReviews(Long driverId, Integer rating);
}
