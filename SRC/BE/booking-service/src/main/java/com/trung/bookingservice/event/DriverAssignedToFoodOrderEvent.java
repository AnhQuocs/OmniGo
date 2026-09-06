package com.trung.bookingservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignedToFoodOrderEvent implements Serializable {
    private Long orderId;
    private Long driverId;
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();
}
