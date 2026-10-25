package com.trung.bookingservice.service.client;

import com.trung.bookingservice.dto.request.PricingRequest;
import com.trung.bookingservice.dto.response.PricingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "pricing-service")
public interface PricingClient {

    @PostMapping("/api/v1/pricing/calculate")
    PricingResponse calculatePrice(
            @RequestBody PricingRequest request,
            @RequestParam("nearbyDriversCount") int nearbyDriversCount
    );

    @PostMapping("/api/v1/pricing/vouchers/rollback")
    void rollbackVoucher(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "orderId", required = false) String orderId
    );
}