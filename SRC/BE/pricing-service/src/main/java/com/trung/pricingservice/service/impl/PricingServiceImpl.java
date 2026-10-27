package com.trung.pricingservice.service.impl;

import com.trung.pricingservice.dto.request.DeliveryFeeRequest;
import com.trung.pricingservice.dto.request.PricingConfigRequest;
import com.trung.pricingservice.dto.request.PricingRequest;
import com.trung.pricingservice.dto.response.PricingResponse;
import com.trung.pricingservice.entity.PricingConfig;
import com.trung.pricingservice.mapper.PricingMapper;
import com.trung.pricingservice.repository.PricingConfigRepository;
import com.trung.pricingservice.service.RouteCalculationService;
import com.trung.pricingservice.service.PricingService;
import com.trung.pricingservice.util.enums.SurgeLevel;
import com.uber.h3core.H3Core;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.trung.pricingservice.dto.route.RouteResult;
import com.trung.pricingservice.dto.response.DeliveryFeeResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PricingConfigRepository configRepository;
    private final PricingMapper pricingMapper;
    private final H3Core h3Core;
    private final RouteCalculationService routeCalculationService;

    private static final int H3_RESOLUTION = 8; // Bán kính ô lục giác khoảng 700 mét
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public PricingResponse calculateDynamicPrice(PricingRequest request, int nearbyDriversCount) {
        // Phân vùng điểm đón khách hàng vào ô lục giác H3
        String h3Index = h3Core.latLngToCellAddress(request.getStartLatitude(), request.getStartLongitude(), H3_RESOLUTION);

        // Ghi nhận lượt Cầu (Demand) có khử trùng lặp theo customerId / tọa độ điểm đón trong 5 phút
        String customerIdentifier = request.getCustomerId() != null
                ? request.getCustomerId().toString()
                : ("loc:" + Math.round(request.getStartLatitude() * 1000) + "_" + Math.round(request.getStartLongitude() * 1000));
        configRepository.recordDemand(h3Index, customerIdentifier);
        int totalDemand = configRepository.getDemand(h3Index);


        String vehicleType = RouteCalculationService.normalizeVehicleType(request.getVehicleType());

        // Lấy khoảng cách & thời gian thực tế qua OSRM theo loại xe
        RouteResult route = routeCalculationService.getRouteInfo(
                request.getStartLatitude(), request.getStartLongitude(),
                request.getEndLatitude(), request.getEndLongitude(),
                vehicleType
        );
        double distance = route.getDistanceKm();
        int estimatedDuration = route.getDurationMinutes();

        // Lấy cấu hình bảng giá gốc từ Redis
        PricingConfig baseConfig = configRepository.getBasePricingConfig();

        SurgeLevel surgeLevel = determineSurgeLevel(totalDemand, nearbyDriversCount);
        double surgeMultiplier = getSurgeMultiplier(surgeLevel, baseConfig);

        // Hệ số phân tầng theo loại xe (lấy động từ Redis config)
        double vehicleMultiplier = getVehicleMultiplier(vehicleType, baseConfig);

        // Tính toán chi phí phân tầng
        double distanceFare = distance * baseConfig.getPricePerKm() * vehicleMultiplier;
        double durationFare = estimatedDuration * baseConfig.getPricePerMinute() * vehicleMultiplier;

        double rawTotal = (baseConfig.getBaseFare() * vehicleMultiplier) + distanceFare + durationFare;
        double finalPrice = Math.round((rawTotal * surgeMultiplier) / 1000.0) * 1000.0;

        log.info("[H3-Surge] Cell: {} | Xe: {} ({}x) | Cầu: {} | Cung: {} -> Mức: {} ({}x) | Quãng đường: {} km (Nguồn: {})",
                h3Index, vehicleType, vehicleMultiplier, totalDemand, nearbyDriversCount, surgeLevel.name(), surgeMultiplier, distance, route.getSource());

        return pricingMapper.toPricingResponse(
                baseConfig, distanceFare, durationFare, surgeLevel, surgeMultiplier, finalPrice, distance, estimatedDuration, h3Index
        );
    }

    private double getVehicleMultiplier(String vehicleType, PricingConfig config) {
        if ("CAR_4_SEAT".equals(vehicleType)) return config.getCar4SeatMultiplier();
        if ("CAR_7_SEAT".equals(vehicleType)) return config.getCar7SeatMultiplier();
        return config.getBikeMultiplier();
    }

    private double getSurgeMultiplier(SurgeLevel surgeLevel, PricingConfig config) {
        return switch (surgeLevel) {
            case SEVERE -> config.getSurgeSevereMultiplier();
            case MODERATE -> config.getSurgeModerateMultiplier();
            case LIGHT -> config.getSurgeLightMultiplier();
            default -> 1.0;
        };
    }

    @Override
    public DeliveryFeeResponse calculateDeliveryFee(DeliveryFeeRequest request) {
        RouteResult route = routeCalculationService.getRouteInfo(
                request.getRestaurantLatitude(), request.getRestaurantLongitude(),
                request.getCustomerLatitude(), request.getCustomerLongitude(),
                "BIKE"
        );

        PricingConfig baseConfig = configRepository.getBasePricingConfig();
        double distanceKm = route.getDistanceKm();

        // Biểu phí ship đồ ăn tính toán động theo cấu hình Redis
        double fee = baseConfig.getDeliveryBaseFare();
        if (distanceKm > baseConfig.getDeliveryBaseKm()) {
            fee += (distanceKm - baseConfig.getDeliveryBaseKm()) * baseConfig.getDeliveryPricePerKm();
        }
        double roundedFee = Math.round(fee / 1000.0) * 1000.0;

        log.info("[Food-Delivery-Fee] Quán ({}, {}) -> Khách ({}, {}) | Quãng đường: {} km | Phí ship: {} đ (Nguồn: {})",
                request.getRestaurantLatitude(), request.getRestaurantLongitude(),
                request.getCustomerLatitude(), request.getCustomerLongitude(),
                distanceKm, roundedFee, route.getSource());

        return DeliveryFeeResponse.builder()
                .distanceInKm(distanceKm)
                .estimatedDurationMinutes(route.getDurationMinutes())
                .deliveryFee(roundedFee)
                .calculationSource(route.getSource())
                .build();
    }

    @Override
    public PricingConfig updateBasePricingConfig(PricingConfigRequest request) {
        PricingConfig current = configRepository.getBasePricingConfig();
        PricingConfig newConfig = PricingConfig.builder()
                .baseFare(request.getBaseFare() != null ? request.getBaseFare() : current.getBaseFare())
                .pricePerKm(request.getPricePerKm() != null ? request.getPricePerKm() : current.getPricePerKm())
                .pricePerMinute(request.getPricePerMinute() != null ? request.getPricePerMinute() : current.getPricePerMinute())
                .bikeMultiplier(request.getBikeMultiplier() != null ? request.getBikeMultiplier() : current.getBikeMultiplier())
                .car4SeatMultiplier(request.getCar4SeatMultiplier() != null ? request.getCar4SeatMultiplier() : current.getCar4SeatMultiplier())
                .car7SeatMultiplier(request.getCar7SeatMultiplier() != null ? request.getCar7SeatMultiplier() : current.getCar7SeatMultiplier())
                .deliveryBaseFare(request.getDeliveryBaseFare() != null ? request.getDeliveryBaseFare() : current.getDeliveryBaseFare())
                .deliveryBaseKm(request.getDeliveryBaseKm() != null ? request.getDeliveryBaseKm() : current.getDeliveryBaseKm())
                .deliveryPricePerKm(request.getDeliveryPricePerKm() != null ? request.getDeliveryPricePerKm() : current.getDeliveryPricePerKm())
                .haversineFallbackMultiplier(request.getHaversineFallbackMultiplier() != null ? request.getHaversineFallbackMultiplier() : current.getHaversineFallbackMultiplier())
                .minDistanceBypassMeters(request.getMinDistanceBypassMeters() != null ? request.getMinDistanceBypassMeters() : current.getMinDistanceBypassMeters())
                .surgeLightMultiplier(request.getSurgeLightMultiplier() != null ? request.getSurgeLightMultiplier() : current.getSurgeLightMultiplier())
                .surgeModerateMultiplier(request.getSurgeModerateMultiplier() != null ? request.getSurgeModerateMultiplier() : current.getSurgeModerateMultiplier())
                .surgeSevereMultiplier(request.getSurgeSevereMultiplier() != null ? request.getSurgeSevereMultiplier() : current.getSurgeSevereMultiplier())
                .build();

        configRepository.updatePricingConfig(newConfig);
        log.info("Đã cập nhật cấu hình bảng giá mới vào Redis: {}", newConfig);
        return newConfig;
    }

    @Override
    public PricingConfig getBasePricingConfig() {
        return configRepository.getBasePricingConfig();
    }

    private SurgeLevel determineSurgeLevel(int demand, int supply) {
        if (supply == 0 && demand > 0) return SurgeLevel.SEVERE;

        double ratio = (double) demand / Math.max(supply, 1);

        if (ratio > 3.0) return SurgeLevel.SEVERE;
        if (ratio > 1.8) return SurgeLevel.MODERATE;
        if (ratio > 1.2) return SurgeLevel.LIGHT;
        return SurgeLevel.NORMAL;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}