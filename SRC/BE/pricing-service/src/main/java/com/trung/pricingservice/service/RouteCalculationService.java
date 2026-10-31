package com.trung.pricingservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trung.pricingservice.dto.route.RouteResult;
import com.trung.pricingservice.repository.PricingConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Locale;

@Service
@Slf4j
public class RouteCalculationService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final String osrmCarUrl;
    private final String osrmBikeUrl;
    private final String osrmPublicUrl;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final PricingConfigRepository configRepository;

    public RouteCalculationService(
            @Value("${osrm.car-url:${osrm.url:http://localhost:5000}}") String osrmCarUrl,
            @Value("${osrm.bike-url:http://localhost:5001}") String osrmBikeUrl,
            @Value("${osrm.public-url:https://router.project-osrm.org}") String osrmPublicUrl,
            @Value("${osrm.connect-timeout-ms:2000}") int connectTimeout,
            @Value("${osrm.read-timeout-ms:2500}") int readTimeout,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            PricingConfigRepository configRepository) {

        this.osrmCarUrl = osrmCarUrl != null ? osrmCarUrl.trim() : "http://localhost:5000";
        this.osrmBikeUrl = osrmBikeUrl != null ? osrmBikeUrl.trim() : "http://localhost:5001";
        this.osrmPublicUrl = osrmPublicUrl != null ? osrmPublicUrl.trim() : "https://router.project-osrm.org";
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.configRepository = configRepository;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public static String normalizeVehicleType(String vehicleType) {
        if (vehicleType == null || vehicleType.isBlank()) {
            return "BIKE";
        }
        String upper = vehicleType.trim().toUpperCase();
        if (upper.equals("CAR_4_SEATS") || upper.equals("CAR_4_SEAT") || upper.equals("CAR_4") || upper.equals("CAR4")) {
            return "CAR_4_SEAT";
        }
        if (upper.equals("CAR_7_SEATS") || upper.equals("CAR_7_SEAT") || upper.equals("CAR_7") || upper.equals("CAR7")) {
            return "CAR_7_SEAT";
        }
        if (upper.equals("EXPRESS") || upper.equals("DELIVERY") || upper.equals("FOOD")) {
            return "EXPRESS";
        }
        if (upper.equals("DRIVE") || upper.equals("CAR")) {
            return "CAR_4_SEAT";
        }
        return "BIKE";
    }

    public static boolean isCar(String normalizedVehicle) {
        return "CAR_4_SEAT".equalsIgnoreCase(normalizedVehicle) || "CAR_7_SEAT".equalsIgnoreCase(normalizedVehicle);
    }


    public static String getOsrmProfile(String normalizedVehicle) {
        return isCar(normalizedVehicle) ? "driving" : "driving";
    }

    public static int calculateRealisticDuration(double distanceKm, double osrmDurationSec, String normalizedVehicle) {
        if (distanceKm <= 0.05) {
            return 1;
        }
        boolean car = isCar(normalizedVehicle);
        double osrmMin = osrmDurationSec / 60.0;
        // Vận tốc thực tế đô thị (Hà Nội): Xe máy ~28.5 km/h, Ô tô ~24.0 km/h
        double realisticSpeedKmH = car ? 24.0 : 28.5;
        double speedDurationMin = (distanceKm / realisticSpeedKmH) * 60.0;

        // Kết hợp: lấy giá trị lớn hơn giữa (thời gian OSRM nhân hệ số đô thị 1.8x) và thời gian theo vận tốc thực tế
        double adjustedMin = Math.max(osrmMin * 1.8, speedDurationMin);
        return Math.max(1, (int) Math.round(adjustedMin));
    }

    public RouteResult getRouteInfo(double startLat, double startLng, double endLat, double endLng, String vehicleType) {
        String normalizedVehicle = normalizeVehicleType(vehicleType);

        // 1. Nếu điểm đón và trả quá gần (< minDistanceBypassMeters), trả về cước mở cửa và bỏ qua gọi API
        double minBypassMeters = configRepository != null ? configRepository.getBasePricingConfig().getMinDistanceBypassMeters() : 50.0;
        double minBypassKm = Math.max(0.01, minBypassMeters / 1000.0);
        double directDistanceKm = calculateHaversineDistance(startLat, startLng, endLat, endLng);
        if (directDistanceKm < minBypassKm) {
            log.info("[RouteService] Điểm đón và trả quá gần ({} m < {} m), trả về cước tối thiểu.",
                    Math.round(directDistanceKm * 1000.0), Math.round(minBypassMeters));
            return RouteResult.builder()
                    .distanceKm(minBypassKm)
                    .durationMinutes(1)
                    .encodedPolyline(null)
                    .source("MIN_DISTANCE_BYPASS")
                    .build();
        }

        // 2. Kiểm tra cache Redis theo loại xe cụ thể
        String cacheKey = String.format(Locale.ROOT, "route:%.3f,%.3f:%.3f,%.3f:%s", startLat, startLng, endLat, endLng, normalizedVehicle);

        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                String[] parts = cached.split("::");
                if (parts.length >= 2) {
                    double dist = Double.parseDouble(parts[0]);
                    int dur = Integer.parseInt(parts[1]);
                    String polyline = parts.length > 2 ? parts[2] : null;

                    return RouteResult.builder()
                            .distanceKm(dist)
                            .durationMinutes(dur)
                            .encodedPolyline(polyline)
                            .source("REDIS_CACHE")
                            .build();
                }
            }
        } catch (Exception e) {
            log.warn("[RouteService] Lỗi khi đọc cache Redis: {}", e.getMessage());
        }

        // 3. Ưu tiên 1: Gọi Local OSRM Docker (cực nhanh, offline, không tốn phí)
        String localUrl = isCar(normalizedVehicle) ? osrmCarUrl : osrmBikeUrl;
        RouteResult route = callOsrm(localUrl, startLat, startLng, endLat, endLng, normalizedVehicle, "OSRM_LOCAL");
        if (route != null) {
            cacheRoute(cacheKey, route, Duration.ofHours(2));
            return route;
        }

        // 4. Ưu tiên 2: Fallback sang Public OSRM nếu tọa độ ngoài vùng bản đồ local
        route = callOsrm(osrmPublicUrl, startLat, startLng, endLat, endLng, normalizedVehicle, "OSRM_PUBLIC");
        if (route != null) {
            cacheRoute(cacheKey, route, Duration.ofHours(2));
            return route;
        }

        // 5. Fallback cuối cùng sang công thức Haversine * hệ số (cache 1 phút)
        RouteResult fallback = calculateFallbackRoute(startLat, startLng, endLat, endLng, normalizedVehicle);
        cacheRoute(cacheKey, fallback, Duration.ofMinutes(1));
        return fallback;
    }

    private RouteResult callOsrm(String baseUrl, double startLat, double startLng, double endLat, double endLng, String normalizedVehicle, String sourceLabel) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        try {
            String cleanBaseUrl = baseUrl.replaceAll("/+$", "");
            String profile = getOsrmProfile(normalizedVehicle);
            String url = String.format(Locale.ROOT, "%s/route/v1/%s/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=polyline",
                    cleanBaseUrl, profile, startLng, startLat, endLng, endLat);

            String responseBody = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            if (responseBody != null && !responseBody.isBlank()) {
                JsonNode root = objectMapper.readTree(responseBody);
                String code = root.path("code").asText("");
                if ("Ok".equalsIgnoreCase(code)) {
                    JsonNode routes = root.path("routes");
                    if (routes.isArray() && !routes.isEmpty()) {
                        JsonNode firstRoute = routes.get(0);
                        double distanceMeters = firstRoute.path("distance").asDouble(0.0);
                        double osrmDurationSec = firstRoute.path("duration").asDouble(0.0);
                        String polyline = firstRoute.path("geometry").asText(null);

                        double distanceKm = Math.round((distanceMeters / 1000.0) * 100.0) / 100.0;

                        // Hiệu chỉnh thời gian di chuyển thực tế theo điều kiện giao thông đô thị Việt Nam:
                        // OSRM Public trả về thời gian lý thuyết (free-flow) theo chuẩn cao tốc châu Âu (tốc độ ~55-80 km/h),
                        // Trong khi thực tế đô thị (đèn đỏ, nút giao, mật độ xe đông):
                        // - Xe máy: trung bình khoảng 28.5 km/h
                        // - Ô tô: trung bình khoảng 24.0 km/h
                        int durationMinutes = calculateRealisticDuration(distanceKm, osrmDurationSec, normalizedVehicle);


                        log.info("[RouteService] {} thành công | Xe: {} | {} km | {} phút | Polyline len: {}",
                                sourceLabel, normalizedVehicle, distanceKm, durationMinutes, polyline != null ? polyline.length() : 0);

                        return RouteResult.builder()
                                .distanceKm(distanceKm)
                                .durationMinutes(durationMinutes)
                                .encodedPolyline(polyline)
                                .source(sourceLabel)
                                .build();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[RouteService] Gọi {} ({}) thất bại: {}", sourceLabel, baseUrl, e.getMessage());
        }
        return null;
    }

    private void cacheRoute(String cacheKey, RouteResult result, Duration duration) {
        if (result == null) return;
        try {
            String val = result.getDistanceKm() + "::" + result.getDurationMinutes() + "::" + (result.getEncodedPolyline() != null ? result.getEncodedPolyline() : "");
            redisTemplate.opsForValue().set(cacheKey, val, duration);
        } catch (Exception e) {
            log.warn("[RouteService] Không thể lưu cache Redis: {}", e.getMessage());
        }
    }

    private RouteResult calculateFallbackRoute(double startLat, double startLng, double endLat, double endLng, String normalizedVehicle) {
        double haversine = calculateHaversineDistance(startLat, startLng, endLat, endLng);
        double fallbackMultiplier = configRepository != null ? configRepository.getBasePricingConfig().getHaversineFallbackMultiplier() : 1.35;
        double fallbackKm = Math.round((haversine * fallbackMultiplier) * 100.0) / 100.0;
        double speedKmH = isCar(normalizedVehicle) ? 24.0 : 28.5;
        int fallbackMinutes = Math.max(1, (int) Math.ceil((fallbackKm / speedKmH) * 60.0));

        return RouteResult.builder()
                .distanceKm(fallbackKm)
                .durationMinutes(fallbackMinutes)
                .encodedPolyline(null)
                .source("FALLBACK_HAVERSINE")
                .build();
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
