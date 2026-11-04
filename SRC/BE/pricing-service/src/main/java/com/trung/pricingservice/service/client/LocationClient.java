package com.trung.pricingservice.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "location-service")
public interface LocationClient {
    @GetMapping("/api/v1/internal/locations/drivers/nearby")
    List<NearbyDriver> getNearbyDrivers(@RequestParam("longitude") double longitude,
                                      @RequestParam("latitude") double latitude,
                                      @RequestParam("radius") double radius);

    record NearbyDriver(Long driverId) {}
}
