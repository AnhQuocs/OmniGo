package com.trung.bookingservice.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverBookingReviewSummaryResponse {

    private Long driverId;
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingCounts;
    private List<BookingReviewResponse> reviews;
}

