package com.trung.bookingservice.service.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
@FeignClient(name = "payment-service")
public interface PaymentClient {
    @PostMapping("/api/v1/payments/internal/drivers/{driverId}/booking-fees")
    BigDecimal getBookingFees(@PathVariable("driverId") Long driverId, @RequestBody List<Long> bookingIds);
}
