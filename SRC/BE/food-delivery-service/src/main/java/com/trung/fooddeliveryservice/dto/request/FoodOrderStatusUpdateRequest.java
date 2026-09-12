package com.trung.fooddeliveryservice.dto.request;

import com.trung.fooddeliveryservice.util.enums.OrderStatus;
import com.trung.fooddeliveryservice.util.enums.OrderCancelReason;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderStatusUpdateRequest {

    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private OrderStatus status;

    private String reason;

    private OrderCancelReason reasonCode;
}
