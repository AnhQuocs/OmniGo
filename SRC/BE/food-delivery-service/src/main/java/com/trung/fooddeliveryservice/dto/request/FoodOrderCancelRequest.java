package com.trung.fooddeliveryservice.dto.request;

import com.trung.fooddeliveryservice.util.enums.OrderCancelReason;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderCancelRequest {
    private String reason;
    private OrderCancelReason reasonCode;
}
