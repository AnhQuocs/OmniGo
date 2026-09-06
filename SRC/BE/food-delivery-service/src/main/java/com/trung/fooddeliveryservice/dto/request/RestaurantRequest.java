package com.trung.fooddeliveryservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRequest {

    @NotBlank(message = "Tên nhà hàng không được để trống")
    private String name;

    private String phone;

    @NotBlank(message = "Địa chỉ nhà hàng không được để trống")
    private String address;

    @NotNull(message = "Tọa độ vĩ độ (latitude) không được để trống")
    private Double latitude;

    @NotNull(message = "Tọa độ kinh độ (longitude) không được để trống")
    private Double longitude;

    private String imageUrl;

    private String openTime;

    private String closeTime;
}
