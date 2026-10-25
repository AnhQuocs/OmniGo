package com.trung.pricingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryFeeResponse {
    private double distanceInKm;
    private int estimatedDurationMinutes;
    private double deliveryFee;
    private String calculationSource;
}
