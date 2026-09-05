package com.trung.userdriverservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAdminUpdateRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Số điện thoại phải bao gồm đúng 10 chữ số (bắt đầu bằng 03, 05, 07, 08, 09)")
    private String phoneNumber;

    @Email(message = "Địa chỉ email không đúng định dạng")
    private String email;

    @NotBlank(message = "Loại phương tiện không được để trống")
    private String vehicleType;

    @NotBlank(message = "Biển số xe không được để trống")
    @Pattern(regexp = "^[0-9]{2}[A-Z0-9]{1,3}[-\\s]?[0-9]{3,5}(\\.[0-9]{2})?$", message = "Biển số xe không đúng định dạng (Ví dụ: 29A-123.45, 51F-888.88, 29B1-567.89)")
    private String licensePlate;

    private String vehicleModel;
}

