package com.trung.fooddeliveryservice.dto.response;

import com.trung.fooddeliveryservice.util.enums.OrderStatus;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderResponse implements Serializable {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private Long driverId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private BigDecimal deliveryFee;
    private String deliveryAddress;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String note;
    private String paymentMethod;
    private Boolean isPaid;
    private List<FoodOrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
