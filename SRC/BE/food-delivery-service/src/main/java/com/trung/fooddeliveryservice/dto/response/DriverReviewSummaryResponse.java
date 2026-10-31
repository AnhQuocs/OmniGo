package com.trung.fooddeliveryservice.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverReviewSummaryResponse {

    private Long driverId;
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingCounts;
    private Page<FoodReviewResponse> reviews;
}

