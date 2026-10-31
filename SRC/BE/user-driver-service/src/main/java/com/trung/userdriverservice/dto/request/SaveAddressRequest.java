package com.trung.userdriverservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveAddressRequest {

    @NotBlank(message = "Tên nhãn địa chỉ không được để trống (VD: Nhà riêng, Công ty)")
    private String label;

    @Builder.Default
    private String addressType = "OTHER";

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String address;

    @NotNull(message = "Vĩ độ (latitude) không được để trống")
    private Double latitude;

    @NotNull(message = "Kinh độ (longitude) không được để trống")
    private Double longitude;

    @Builder.Default
    private Boolean isDefault = false;
}

