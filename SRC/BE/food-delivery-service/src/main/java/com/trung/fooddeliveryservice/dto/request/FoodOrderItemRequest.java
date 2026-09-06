package com.trung.fooddeliveryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderItemRequest {

    @NotNull(message = "ID món ăn không được để trống")
    private Long menuItemId;

    @NotNull(message = "Số lượng món ăn không được để trống")
    @Min(value = 1, message = "Số lượng món ăn tối thiểu là 1")
    private Integer quantity;
}
