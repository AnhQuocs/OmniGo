package com.trung.bookingservice.service.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "payment-service", configuration = PaymentClient.Auth.class)
public interface PaymentClient {
    class Auth {
        @Bean
        public RequestInterceptor internalAuth(@Value("${app.internal-api-token:${INTERNAL_API_TOKEN:omnipay-internal-shared-secret-token-32bytes-min}}") String token) {
            return template -> template.header("X-Internal-Token", token);
        }
    }
    @PostMapping("/api/v1/payments/internal/drivers/{driverId}/booking-fees")
    BigDecimal getBookingFees(@PathVariable("driverId") Long driverId, @RequestBody List<Long> bookingIds);
}
