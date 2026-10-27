package com.trung.bookingservice.service;

import com.trung.bookingservice.dto.response.ApiResponse;
import com.trung.bookingservice.dto.response.DriverProfileResponse;
import com.trung.bookingservice.service.client.UserDriverClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverVehicleService {

    private final UserDriverClient userDriverClient;
    private final StringRedisTemplate redisTemplate;

    private static final String VEHICLE_CACHE_PREFIX = "driver:vehicle_type:";

    public String getDriverVehicleType(Long driverId) {
        if (driverId == null) return null;

        String cacheKey = VEHICLE_CACHE_PREFIX + driverId;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Lỗi đọc cache vehicleType tài xế {}: {}", driverId, e.getMessage());
        }

        try {
            ResponseEntity<ApiResponse<DriverProfileResponse>> response = userDriverClient.getDriverProfile(driverId);
            if (response != null && response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null && response.getBody().getData() != null) {
                String vType = response.getBody().getData().vehicleType();
                if (vType != null && !vType.isBlank()) {
                    try {
                        redisTemplate.opsForValue().set(cacheKey, vType, 10, TimeUnit.MINUTES);
                    } catch (Exception e) {
                        log.warn("Lỗi ghi cache vehicleType tài xế {}: {}", driverId, e.getMessage());
                    }
                    return vType;
                }
            }
        } catch (FeignException e) {
            log.warn("Feign error khi lấy thông tin xe của tài xế {}: HTTP {}", driverId, e.status());
        } catch (Exception e) {
            log.warn("Lỗi khi lấy thông tin xe của tài xế {}: {}", driverId, e.getMessage());
        }

        return null;
    }

    public String normalizeVehicleType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return "CAR_4_SEAT";
        }
        String upper = rawType.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (upper.contains("BIKE") || upper.contains("MAY") || upper.contains("MOTO")) {
            return "BIKE";
        }
        if (upper.contains("7") || upper.contains("BAY") || upper.contains("SEVEN")) {
            return "CAR_7_SEAT";
        }
        if (upper.contains("4") || upper.contains("BON") || upper.contains("FOUR") || upper.contains("CAR")) {
            return "CAR_4_SEAT";
        }
        if (upper.contains("EXPRESS") || upper.contains("DELIVERY") || upper.contains("FOOD")) {
            return "EXPRESS";
        }
        return upper;
    }

    public boolean isVehicleTypeMatching(String requestedType, String driverVehicleType) {
        if (requestedType == null || requestedType.isBlank()) {
            return true;
        }
        if (driverVehicleType == null || driverVehicleType.isBlank()) {
            return false;
        }
        String normReq = normalizeVehicleType(requestedType);
        String normDriver = normalizeVehicleType(driverVehicleType);
        return normReq.equalsIgnoreCase(normDriver);
    }

    public boolean isEligibleForFoodDelivery(String driverVehicleType) {
        if (driverVehicleType == null || driverVehicleType.isBlank()) {
            return false;
        }
        String norm = normalizeVehicleType(driverVehicleType);
        return "BIKE".equalsIgnoreCase(norm) || "EXPRESS".equalsIgnoreCase(norm);
    }
}
