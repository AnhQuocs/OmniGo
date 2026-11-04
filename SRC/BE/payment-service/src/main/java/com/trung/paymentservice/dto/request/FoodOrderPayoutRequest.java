package com.trung.paymentservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderPayoutRequest {
    private Long restaurantOwnerId;
    private BigDecimal foodAmount;
    private Long driverId;
    private Long orderId;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;
    private String paymentMethod;
}
