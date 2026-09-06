package com.trung.fooddeliveryservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderCreateRequest {

    @NotNull(message = "ID nhà hàng không được để trống")
    private Long restaurantId;

    private String customerName;

    private String customerPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String deliveryAddress;

    @NotNull(message = "Tọa độ vĩ độ (latitude) giao hàng không được để trống")
    private Double deliveryLatitude;

    @NotNull(message = "Tọa độ kinh độ (longitude) giao hàng không được để trống")
    private Double deliveryLongitude;

    private String note;

    @Builder.Default
    private String paymentMethod = "CASH";

    @NotEmpty(message = "Đơn hàng phải có ít nhất một món ăn")
    @Valid
    private List<FoodOrderItemRequest> items;
}
