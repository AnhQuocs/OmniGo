package com.trung.fooddeliveryservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Snapshot of a persisted order, shared with the booking WebSocket relay. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderStatusEvent {
    private Long orderId;
    private String status;
    private String timestamp;
}
