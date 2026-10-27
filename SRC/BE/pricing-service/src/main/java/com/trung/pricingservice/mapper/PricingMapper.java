package com.trung.pricingservice.mapper;

import com.trung.pricingservice.dto.response.PricingResponse;
import com.trung.pricingservice.entity.PricingConfig;
import com.trung.pricingservice.util.enums.SurgeLevel;
import org.springframework.stereotype.Component;

@Component
public class PricingMapper {

    public PricingResponse toPricingResponse(
            PricingConfig config,
            double distanceFare,
            double durationFare,
            SurgeLevel level,
            double totalPrice,
            double distance,
            int duration,
            String h3Index) {

        return toPricingResponse(config, distanceFare, durationFare, level, level.getMultiplier(), totalPrice, distance, duration, h3Index);
    }

    public PricingResponse toPricingResponse(
            PricingConfig config,
            double distanceFare,
            double durationFare,
            SurgeLevel level,
            double surgeMultiplier,
            double totalPrice,
            double distance,
            int duration,
            String h3Index) {

        return PricingResponse.builder()
                .baseFare(config.getBaseFare())
                .distanceFare(distanceFare)
                .durationFare(durationFare)
                .surgeMultiplier(surgeMultiplier)
                .surgeLevel(level.name())
                .totalPrice(totalPrice)
                .distanceInKm(distance)
                .estimatedDurationMinutes(duration)
                .h3Index(h3Index)
                .build();
    }
}