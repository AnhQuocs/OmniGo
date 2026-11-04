package com.trung.bookingservice.dto.response;

import com.trung.bookingservice.util.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Builder
@Setter
public class BookingResponse {
    private Long bookingId;
    private Long customerId;
    private String customerName;
    private Long driverId;
    private String customerAvatarUrl;
    private String driverAvatarUrl;
    private Double startLongitude;
    private Double startLatitude;
    private Double endLongitude;
    private Double endLatitude;
    private BookingStatus status;
    private Double distanceInKm;
    private Double price;
    private String vehicleType;
    private String paymentMethod;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private String voucherCode;
    private boolean prepaid;
    private boolean paid;
    private String pickupAddress;
    private String dropoffAddress;
    private LocalDateTime createdAt;
}
