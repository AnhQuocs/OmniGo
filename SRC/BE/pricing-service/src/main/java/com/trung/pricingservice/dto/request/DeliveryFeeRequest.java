package com.trung.pricingservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryFeeRequest {
    @NotNull(message = "Vĩ độ nhà hàng không được để trống")
    private Double restaurantLatitude;

    @NotNull(message = "Kinh độ nhà hàng không được để trống")
    private Double restaurantLongitude;

    @NotNull(message = "Vĩ độ điểm giao không được để trống")
    private Double customerLatitude;

    @NotNull(message = "Kinh độ điểm giao không được để trống")
    private Double customerLongitude;
}
