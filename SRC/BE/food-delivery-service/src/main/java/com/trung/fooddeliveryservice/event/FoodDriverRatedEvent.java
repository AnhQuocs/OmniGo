package com.trung.fooddeliveryservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodDriverRatedEvent implements Serializable {
    private Long orderId;
    private Long driverId;
    private Long customerId;
    private Integer rating;
    private String comment;
}
