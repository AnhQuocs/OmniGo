package com.trung.bookingservice.controller;

import com.trung.bookingservice.service.DriverVehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/drivers")
@RequiredArgsConstructor
@Slf4j
public class BookingInternalController {

    private final DriverVehicleService driverVehicleService;

    @PutMapping("/{driverId}/vehicle-cache")
    public ResponseEntity<Void> syncDriverVehicleCache(
            @PathVariable Long driverId,
            @RequestParam(value = "vehicleType", required = false) String vehicleType) {
        log.info("Nhận yêu cầu đồng bộ cache phương tiện cho tài xế {}: {}", driverId, vehicleType);
        if (vehicleType != null && !vehicleType.isBlank()) {
            driverVehicleService.updateVehicleTypeCache(driverId, vehicleType);
        } else {
            driverVehicleService.evictVehicleTypeCache(driverId);
        }
        return ResponseEntity.ok().build();
    }
}

