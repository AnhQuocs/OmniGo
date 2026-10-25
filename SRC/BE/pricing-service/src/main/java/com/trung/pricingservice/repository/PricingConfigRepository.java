package com.trung.pricingservice.repository;

import com.trung.pricingservice.entity.PricingConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PricingConfigRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_KEY = "pricing:config";

    public PricingConfig getBasePricingConfig() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(REDIS_KEY);

        PricingConfig defaults = PricingConfig.builder().build();

        if (entries.isEmpty()) {
            return defaults;
        }

        return PricingConfig.builder()
                .baseFare(getDoubleOrDefault(entries, "baseFare", defaults.getBaseFare()))
                .pricePerKm(getDoubleOrDefault(entries, "pricePerKm", defaults.getPricePerKm()))
                .pricePerMinute(getDoubleOrDefault(entries, "pricePerMinute", defaults.getPricePerMinute()))
                .bikeMultiplier(getDoubleOrDefault(entries, "bikeMultiplier", defaults.getBikeMultiplier()))
                .car4SeatMultiplier(getDoubleOrDefault(entries, "car4SeatMultiplier", defaults.getCar4SeatMultiplier()))
                .car7SeatMultiplier(getDoubleOrDefault(entries, "car7SeatMultiplier", defaults.getCar7SeatMultiplier()))
                .deliveryBaseFare(getDoubleOrDefault(entries, "deliveryBaseFare", defaults.getDeliveryBaseFare()))
                .deliveryBaseKm(getDoubleOrDefault(entries, "deliveryBaseKm", defaults.getDeliveryBaseKm()))
                .deliveryPricePerKm(getDoubleOrDefault(entries, "deliveryPricePerKm", defaults.getDeliveryPricePerKm()))
                .haversineFallbackMultiplier(getDoubleOrDefault(entries, "haversineFallbackMultiplier", defaults.getHaversineFallbackMultiplier()))
                .minDistanceBypassMeters(getDoubleOrDefault(entries, "minDistanceBypassMeters", defaults.getMinDistanceBypassMeters()))
                .surgeLightMultiplier(getDoubleOrDefault(entries, "surgeLightMultiplier", defaults.getSurgeLightMultiplier()))
                .surgeModerateMultiplier(getDoubleOrDefault(entries, "surgeModerateMultiplier", defaults.getSurgeModerateMultiplier()))
                .surgeSevereMultiplier(getDoubleOrDefault(entries, "surgeSevereMultiplier", defaults.getSurgeSevereMultiplier()))
                .build();
    }

    private double getDoubleOrDefault(Map<Object, Object> entries, String key, double defaultValue) {
        Object val = entries.get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(val.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void updatePricingConfig(PricingConfig config) {
        Map<String, String> map = new HashMap<>();
        map.put("baseFare", String.valueOf(config.getBaseFare()));
        map.put("pricePerKm", String.valueOf(config.getPricePerKm()));
        map.put("pricePerMinute", String.valueOf(config.getPricePerMinute()));
        map.put("bikeMultiplier", String.valueOf(config.getBikeMultiplier()));
        map.put("car4SeatMultiplier", String.valueOf(config.getCar4SeatMultiplier()));
        map.put("car7SeatMultiplier", String.valueOf(config.getCar7SeatMultiplier()));
        map.put("deliveryBaseFare", String.valueOf(config.getDeliveryBaseFare()));
        map.put("deliveryBaseKm", String.valueOf(config.getDeliveryBaseKm()));
        map.put("deliveryPricePerKm", String.valueOf(config.getDeliveryPricePerKm()));
        map.put("haversineFallbackMultiplier", String.valueOf(config.getHaversineFallbackMultiplier()));
        map.put("minDistanceBypassMeters", String.valueOf(config.getMinDistanceBypassMeters()));
        map.put("surgeLightMultiplier", String.valueOf(config.getSurgeLightMultiplier()));
        map.put("surgeModerateMultiplier", String.valueOf(config.getSurgeModerateMultiplier()));
        map.put("surgeSevereMultiplier", String.valueOf(config.getSurgeSevereMultiplier()));

        redisTemplate.opsForHash().putAll(REDIS_KEY, map);
    }

    public void updateDemand(String h3Index) {
        String key = "pricing:demand:" + h3Index;
        redisTemplate.opsForValue().increment(key, 1);
        // Tự động hết hạn sau 5 phút để refresh cung cầu liên tục
        redisTemplate.expire(key, Duration.ofMinutes(5));
    }

    public int getDemand(String h3Index) {
        Object val = redisTemplate.opsForValue().get("pricing:demand:" + h3Index);
        return val != null ? Integer.parseInt(val.toString()) : 0;
    }
}