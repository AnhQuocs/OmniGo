package com.trung.bookingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderStatusEvent {
    private Long orderId;
    private String status;
    private String timestamp;
}
