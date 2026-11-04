package com.trung.userdriverservice.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @PutMapping("/api/v1/internal/drivers/{driverId}/vehicle-cache")
    void syncDriverVehicleCache(
            @PathVariable("driverId") Long driverId,
            @RequestParam(value = "vehicleType", required = false) String vehicleType);
}

