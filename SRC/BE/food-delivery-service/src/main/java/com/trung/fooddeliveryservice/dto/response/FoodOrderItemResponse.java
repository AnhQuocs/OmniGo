package com.trung.fooddeliveryservice.dto.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderItemResponse implements Serializable {
    private Long id;
    private Long menuItemId;
    private String itemName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
