package com.trung.userdriverservice.dto.response;

import com.trung.userdriverservice.util.enums.ApprovalStatus;
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
    private Boolean isLocked;
    private String lockedReason;
    private String vehicleType;
    private String licensePlate;
    private String vehicleModel;
    private DriverStatus status;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}