package com.trung.bookingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Wire contract published by food-delivery-service. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderStatusEvent {
    private Long orderId;
    private Long customerId;
    private Long driverId;
    private String status;
    private String timestamp;
}
