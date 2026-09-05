package com.trung.userdriverservice.dto.response;

import com.trung.userdriverservice.util.enums.DriverStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DriverInternalResponse {
    private Long driverId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String vehicleType;
    private String licensePlate;
    private String vehicleModel;
    private DriverStatus status;
    private LocalDateTime createdAt;
}