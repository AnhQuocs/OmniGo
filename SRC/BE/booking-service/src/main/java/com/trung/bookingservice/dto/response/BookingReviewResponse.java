package com.trung.bookingservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingReviewResponse {

    private Long id;
    private Long bookingId;
    private Long driverId;
    private Long customerId;
    private String customerName;
    private String customerAvatarUrl;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

