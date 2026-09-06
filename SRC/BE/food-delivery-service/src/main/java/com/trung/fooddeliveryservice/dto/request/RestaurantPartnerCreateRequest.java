package com.trung.fooddeliveryservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantPartnerCreateRequest {

    // Thông tin nhà hàng
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

    // Thông tin chủ quán / tài khoản đăng nhập
    @NotBlank(message = "Họ tên chủ nhà hàng không được để trống")
    private String ownerName;

    @NotBlank(message = "Số điện thoại chủ nhà hàng không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Số điện thoại chủ quán không hợp lệ (10 chữ số)")
    private String ownerPhone;

    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
}
