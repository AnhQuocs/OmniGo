package com.trung.pricingservice.dto.google;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResult {
    private double distanceKm;
    private int durationMinutes;

    private String encodedPolyline;

    private String source;
}
