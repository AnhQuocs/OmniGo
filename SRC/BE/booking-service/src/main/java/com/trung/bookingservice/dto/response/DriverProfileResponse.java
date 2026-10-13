package com.trung.bookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Only deserialize the fields needed by customers from the internal profile.
@JsonIgnoreProperties(ignoreUnknown = true)
public record DriverProfileResponse(
        Long driverId,
        String fullName,
        String phoneNumber,
        String licensePlate,
        String vehicleType,
        String vehicleModel,
        String avatarUrl) {
}
