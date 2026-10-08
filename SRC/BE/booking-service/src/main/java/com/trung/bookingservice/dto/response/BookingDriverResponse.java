package com.trung.bookingservice.dto.response;

public record BookingDriverResponse(
        Long driverId,
        String driverName,
        String driverPhone,
        String vehiclePlate,
        String vehicleType,
        String vehicleModel,
        String avatarUrl) {
}
