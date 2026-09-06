package com.trung.fooddeliveryservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindDriverForFoodOrderEvent implements Serializable {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private Double restaurantLatitude;
    private Double restaurantLongitude;
    private String dropOffAddress;
    private Double dropOffLatitude;
    private Double dropOffLongitude;
    private BigDecimal totalPrice;
    private BigDecimal deliveryFee;
    private String paymentMethod;
    private Boolean isPaid;
}
