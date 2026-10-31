package com.trung.pricingservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingConfig implements Serializable {
    private static final long serialVersionUID = 1L;

   // 1. Cước cơ bản chuyến xe
    @Builder.Default
    private double baseFare = 10000.0;

    @Builder.Default
    private double pricePerKm = 3600.0;

    @Builder.Default
    private double pricePerMinute = 250.0;

    // 2. Hệ số theo loại phương tiện
    @Builder.Default
    private double bikeMultiplier = 1.0;

    @Builder.Default
    private double car4SeatMultiplier = 1.95;

    @Builder.Default
    private double car7SeatMultiplier = 2.3;

    // 3. Phí giao hàng đồ ăn
    @Builder.Default
    private double deliveryBaseFare = 15000.0;

    @Builder.Default
    private double deliveryBaseKm = 2.0;

    @Builder.Default
    private double deliveryPricePerKm = 5000.0;

    // 4. OSRM Routing & Fallback
    @Builder.Default
    private double haversineFallbackMultiplier = 1.35;

    @Builder.Default
    private double minDistanceBypassMeters = 50.0;

    // 5. Hệ số tăng giá cao điểm (Surge)
    @Builder.Default
    private double surgeLightMultiplier = 1.2;

    @Builder.Default
    private double surgeModerateMultiplier = 1.5;

    @Builder.Default
    private double surgeSevereMultiplier = 2.0;
}
