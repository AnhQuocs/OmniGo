package com.trung.userdriverservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverUpdateRequest {
    @NotBlank(message = "Loại phương tiện không được để trống")
    private String vehicleType;

    @NotBlank(message = "Biển số xe không được để trống")
    @Pattern(regexp = "^[0-9]{2}[A-Z0-9]{1,3}[-\\s]?[0-9]{3,5}(\\.[0-9]{2})?$", message = "Biển số xe không đúng định dạng (Ví dụ: 29A-123.45, 51F-888.88, 29B1-567.89)")
    private String licensePlate;

    private String vehicleModel;
}

