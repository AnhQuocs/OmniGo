package com.trung.userdriverservice.mapper;

import com.trung.userdriverservice.dto.request.DriverRegisterRequest;
import com.trung.userdriverservice.dto.request.UserRegisterRequest;
import com.trung.userdriverservice.dto.response.DriverInternalResponse;
import com.trung.userdriverservice.dto.response.UserPaymentInfoResponse;
import com.trung.userdriverservice.dto.response.UserResponse;
import com.trung.userdriverservice.entity.DriverProfile;
import com.trung.userdriverservice.entity.User;
import com.trung.userdriverservice.repository.DriverProfileRepository;
import com.trung.userdriverservice.util.enums.ApprovalStatus;
import com.trung.userdriverservice.util.enums.DriverStatus;
import com.trung.userdriverservice.util.enums.Role;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final DriverProfileRepository driverProfileRepository;

    public UserMapper(PasswordEncoder passwordEncoder, @Lazy DriverProfileRepository driverProfileRepository) {
        this.passwordEncoder = passwordEncoder;
        this.driverProfileRepository = driverProfileRepository;
    }

    public User toCustomerEntity(UserRegisterRequest request) {
        User user = new User();
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole() != null ? Role.valueOf(request.getRole().toUpperCase()) : Role.CUSTOMER);
        return user;
    }

    public User toDriverEntity(DriverRegisterRequest request) {
        User user = new User();
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Role.DRIVER);
        return user;
    }

    public DriverProfile toDriverProfileEntity(DriverRegisterRequest request, User user) {
        DriverProfile profile = new DriverProfile();
        profile.setUser(user);
        profile.setVehicleType(request.getVehicleType());
        profile.setLicensePlate(request.getLicensePlate());
        profile.setVehicleModel(request.getVehicleModel());
        profile.setCccdNumber(request.getCccdNumber());
        profile.setCccdFrontImage(request.getCccdFrontImage());
        profile.setCccdBackImage(request.getCccdBackImage());
        profile.setGplxNumber(request.getGplxNumber());
        profile.setGplxFrontImage(request.getGplxFrontImage());
        profile.setGplxBackImage(request.getGplxBackImage());
        profile.setStatus(DriverStatus.OFFLINE);
        return profile;
    }

    public UserResponse toUserResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .isLocked(user.getIsLocked())
                .lockedReason(user.getLockedReason())
                .lockedAt(user.getLockedAt())
                .createdAt(user.getCreatedAt());

        if (user.getRole() == Role.DRIVER) {
            driverProfileRepository.findById(user.getId()).ifPresent(dp -> {
                builder.status(dp.getStatus());
                builder.approvalStatus(dp.getApprovalStatus() != null ? dp.getApprovalStatus() : ApprovalStatus.APPROVED);
                builder.rejectionReason(dp.getRejectionReason());
                builder.approvedAt(dp.getApprovedAt());
                builder.vehicleType(dp.getVehicleType());
                builder.licensePlate(dp.getLicensePlate());
                builder.vehicleModel(dp.getVehicleModel());
                builder.cccdNumber(dp.getCccdNumber());
                builder.cccdFrontImage(dp.getCccdFrontImage());
                builder.cccdBackImage(dp.getCccdBackImage());
                builder.gplxNumber(dp.getGplxNumber());
                builder.gplxFrontImage(dp.getGplxFrontImage());
                builder.gplxBackImage(dp.getGplxBackImage());
            });
        }

        return builder.build();
    }

    public DriverInternalResponse toDriverInternalResponse(DriverProfile profile) {
        User user = profile.getUser();
        return DriverInternalResponse.builder()
                .driverId(profile.getDriverId())
                .fullName(user != null ? user.getFullName() : "")
                .phoneNumber(user != null ? user.getPhoneNumber() : "")
                .email(user != null ? user.getEmail() : "")
                .isLocked(user != null ? user.getIsLocked() : false)
                .lockedReason(user != null ? user.getLockedReason() : null)
                .vehicleType(profile.getVehicleType())
                .licensePlate(profile.getLicensePlate())
                .vehicleModel(profile.getVehicleModel())
                .cccdNumber(profile.getCccdNumber())
                .cccdFrontImage(profile.getCccdFrontImage())
                .cccdBackImage(profile.getCccdBackImage())
                .gplxNumber(profile.getGplxNumber())
                .gplxFrontImage(profile.getGplxFrontImage())
                .gplxBackImage(profile.getGplxBackImage())
                .status(profile.getStatus())
                .approvalStatus(profile.getApprovalStatus())
                .rejectionReason(profile.getRejectionReason())
                .approvedAt(profile.getApprovedAt())
                .createdAt(user != null ? user.getCreatedAt() : null)
                .build();
    }

    public UserPaymentInfoResponse toUserPaymentInfoResponse(User user) {
        return UserPaymentInfoResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
