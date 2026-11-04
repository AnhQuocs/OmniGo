package com.trung.bookingservice.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {
    @NotNull(message = "Điểm đón không được để trống")
    private Double startLongitude;

    @NotNull(message = "Điểm đón không được để trống")
    private Double startLatitude;

    @NotNull(message = "Điểm đến không được để trống")
    private Double endLongitude;

    @NotNull(message = "Điểm đến không được để trống")
    private Double endLatitude;

    @Pattern(regexp = "CASH|WALLET")
    private String paymentMethod = "CASH";
    @PositiveOrZero
    private Double expectedPrice;

    private String vehicleType;
    private String pickupAddress;
    private String dropoffAddress;
}