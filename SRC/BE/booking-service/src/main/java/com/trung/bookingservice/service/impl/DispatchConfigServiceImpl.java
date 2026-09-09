package com.trung.bookingservice.service.impl;

import com.trung.bookingservice.dto.request.DispatchConfigRequest;
import com.trung.bookingservice.service.DispatchConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispatchConfigServiceImpl implements DispatchConfigService {

    private static final String REDIS_KEY = "system:config:dispatch";
    private static final String FIELD_MAX_RIDE = "maxRideDistanceKm";
    private static final String FIELD_MAX_FOOD = "maxFoodDeliveryDistanceKm";

    private static final double DEFAULT_MAX_RIDE_KM = 8.0;
    private static final double DEFAULT_MAX_FOOD_KM = 5.5;

    private final StringRedisTemplate redisTemplate;

    @Override
    public Map<String, Double> getDispatchConfig() {
        Map<String, Double> config = new HashMap<>();
        config.put(FIELD_MAX_RIDE, getMaxRideDistanceKm());
        config.put(FIELD_MAX_FOOD, getMaxFoodDeliveryDistanceKm());
        return config;
    }

    @Override
    public Map<String, Double> updateDispatchConfig(DispatchConfigRequest request) {
        redisTemplate.opsForHash().put(REDIS_KEY, FIELD_MAX_RIDE, String.valueOf(request.getMaxRideDistanceKm()));
        redisTemplate.opsForHash().put(REDIS_KEY, FIELD_MAX_FOOD, String.valueOf(request.getMaxFoodDeliveryDistanceKm()));

        log.info("Đã cập nhật cấu hình điều phối: cuốc xe = {} km, giao đồ ăn = {} km",
                request.getMaxRideDistanceKm(), request.getMaxFoodDeliveryDistanceKm());

        return getDispatchConfig();
    }

    @Override
    public Double getMaxRideDistanceKm() {
        return getDoubleField(FIELD_MAX_RIDE, DEFAULT_MAX_RIDE_KM);
    }

    @Override
    public Double getMaxFoodDeliveryDistanceKm() {
        return getDoubleField(FIELD_MAX_FOOD, DEFAULT_MAX_FOOD_KM);
    }

    private Double getDoubleField(String field, double defaultValue) {
        try {
            Object val = redisTemplate.opsForHash().get(REDIS_KEY, field);
            if (val != null) {
                return Double.parseDouble(val.toString());
            }
        } catch (Exception e) {
            log.warn("Không đọc được cấu hình {} từ Redis, dùng mặc định {} km", field, defaultValue);
        }
        return defaultValue;
    }
}
