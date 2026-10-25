package com.trung.pricingservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trung.pricingservice.dto.google.RouteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class GoogleRoutesService {

    private static final String ROUTES_API_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";
    private static final double EARTH_RADIUS_KM = 6371.0;


    private final String apiKey;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final com.trung.pricingservice.repository.PricingConfigRepository configRepository;

    public GoogleRoutesService(
            @Value("${google.maps.api-key:}") String apiKey,
            @Value("${google.maps.connect-timeout-ms:1500}") int connectTimeout,
            @Value("${google.maps.read-timeout-ms:1500}") int readTimeout,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            com.trung.pricingservice.repository.PricingConfigRepository configRepository) {

        this.apiKey = apiKey != null ? apiKey.trim() : "";
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
        if (upper.equals("EXPRESS") || upper.equals("DELIVERY")) {
            return "EXPRESS";
        }
        if (upper.equals("DRIVE") || upper.equals("CAR")) {
            return "CAR_4_SEAT";
        }
        return "BIKE";
    }

    public static String mapToGoogleTravelMode(String normalizedVehicleType) {
        if ("CAR_4_SEAT".equalsIgnoreCase(normalizedVehicleType) || "CAR_7_SEAT".equalsIgnoreCase(normalizedVehicleType)) {
            return "DRIVE";
        }
        return "TWO_WHEELER";
    }

    public RouteResult getRouteInfo(double startLat, double startLng, double endLat, double endLng, String vehicleType) {
        String normalizedVehicle = normalizeVehicleType(vehicleType);
        String googleTravelMode = mapToGoogleTravelMode(normalizedVehicle);

        // 1. Nếu điểm đón và trả quá gần (< minDistanceBypassMeters), trả về cước tối thiểu và bỏ qua gọi Google API
        double minBypassMeters = configRepository != null ? configRepository.getBasePricingConfig().getMinDistanceBypassMeters() : 50.0;
        double minBypassKm = Math.max(0.01, minBypassMeters / 1000.0);
        double directDistanceKm = calculateHaversineDistance(startLat, startLng, endLat, endLng);
        if (directDistanceKm < minBypassKm) {
            log.info("[GoogleRoutes] Điểm đón và trả quá gần ({} m < {} m), trả về cước mở cửa tối thiểu và bỏ qua gọi Google API.",
                    Math.round(directDistanceKm * 1000.0), Math.round(minBypassMeters));
            return RouteResult.builder()
                    .distanceKm(minBypassKm)
                    .durationMinutes(1)
                    .encodedPolyline(null)
                    .source("MIN_DISTANCE_BYPASS")
                    .build();
        }

        // 2. Kiểm tra cache Redis theo loại xe cụ thể (Ô tô đi đường khác, xe máy đi đường khác)
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
            log.warn("[GoogleRoutes] Lỗi khi đọc cache Redis: {}", e.getMessage());
        }

        // 3. Kiểm tra nếu chưa cấu hình API Key thì fallback sang Haversine
        if (apiKey.isBlank()) {
            log.warn("[GoogleRoutes] Chưa cấu hình Google Maps API Key, tự động áp dụng công thức ước lượng Haversine * 1.35 ({})", normalizedVehicle);
            return calculateFallbackRoute(startLat, startLng, endLat, endLng, normalizedVehicle);
        }

        // 4. Chuẩn bị payload gọi Google Routes API v2
        try {

            Map<String, Object> originLocation = Map.of("latLng", Map.of("latitude", startLat, "longitude", startLng));
            Map<String, Object> origin = Map.of("location", originLocation);

            Map<String, Object> destLocation = Map.of("latLng", Map.of("latitude", endLat, "longitude", endLng));
            Map<String, Object> destination = Map.of("location", destLocation);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("origin", origin);
            requestBody.put("destination", destination);
            requestBody.put("travelMode", googleTravelMode);
            requestBody.put("routingPreference", "TRAFFIC_UNAWARE");


            String responseBody = restClient.post()
                    .uri(ROUTES_API_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseBody != null && !responseBody.isBlank()) {
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode routesNode = rootNode.get("routes");

                if (routesNode != null && routesNode.isArray() && !routesNode.isEmpty()) {
                    JsonNode firstRoute = routesNode.get(0);

                    double distanceMeters = firstRoute.has("distanceMeters") ? firstRoute.get("distanceMeters").asDouble() : 0.0;
                    String durationStr = firstRoute.has("duration") ? firstRoute.get("duration").asText() : "0s";
                    int durationSeconds = parseDurationSeconds(durationStr);

                    String encodedPolyline = null;
                    if (firstRoute.has("polyline") && firstRoute.get("polyline").has("encodedPolyline")) {
                        encodedPolyline = firstRoute.get("polyline").get("encodedPolyline").asText();
                    }

                    double distanceKm = Math.round((distanceMeters / 1000.0) * 100.0) / 100.0;
                    int durationMinutes = Math.max(1, (int) Math.ceil(durationSeconds / 60.0));

                    // Lưu cache 2 tiếng
                    try {
                        String cacheVal = distanceKm + "::" + durationMinutes + "::" + (encodedPolyline != null ? encodedPolyline : "");
                        redisTemplate.opsForValue().set(cacheKey, cacheVal, Duration.ofHours(2));
                    } catch (Exception e) {
                        log.warn("[GoogleRoutes] Không thể lưu cache Redis: {}", e.getMessage());
                    }

                    log.info("[GoogleRoutes] Thành công: {} km | {} phút ({}s) | Phương tiện: {} ({})",
                            distanceKm, durationMinutes, durationSeconds, normalizedVehicle, googleTravelMode);

                    return RouteResult.builder()
                            .distanceKm(distanceKm)
                            .durationMinutes(durationMinutes)
                            .encodedPolyline(encodedPolyline)
                            .source("GOOGLE_ROUTES")
                            .build();
                }
            }
        } catch (Exception e) {
            log.warn("[GoogleRoutes] Gọi Routes API thất bại, tự động fallback sang Haversine * 1.35. Lỗi: {}", e.getMessage());
        }

        // 5. Fallback nếu gọi Google lỗi / timeout (cache 1 phút để tránh frontend polling spam Google liên tục)
        RouteResult fallback = calculateFallbackRoute(startLat, startLng, endLat, endLng, normalizedVehicle);
        try {
            String fallbackVal = fallback.getDistanceKm() + "::" + fallback.getDurationMinutes() + "::";
            redisTemplate.opsForValue().set(cacheKey, fallbackVal, Duration.ofMinutes(1));
        } catch (Exception ignored) {}
        return fallback;

    }

    private RouteResult calculateFallbackRoute(double startLat, double startLng, double endLat, double endLng, String normalizedVehicle) {
        double haversine = calculateHaversineDistance(startLat, startLng, endLat, endLng);
        double fallbackMultiplier = configRepository != null ? configRepository.getBasePricingConfig().getHaversineFallbackMultiplier() : 1.35;
        double fallbackKm = Math.round((haversine * fallbackMultiplier) * 100.0) / 100.0;
        // Vận tốc ước tính: Ô tô ~ 25km/h do đèn đỏ & kẹt xe, Xe máy ~ 30km/h
        double speedKmH = ("CAR_4_SEAT".equals(normalizedVehicle) || "CAR_7_SEAT".equals(normalizedVehicle)) ? 25.0 : 30.0;
        int fallbackMinutes = Math.max(1, (int) Math.ceil((fallbackKm / speedKmH) * 60.0));

        return RouteResult.builder()
                .distanceKm(fallbackKm)
                .durationMinutes(fallbackMinutes)
                .encodedPolyline(null)
                .source("FALLBACK_HAVERSINE")
                .build();
    }


    private int parseDurationSeconds(String durationStr) {
        if (durationStr == null || durationStr.isBlank()) return 0;
        try {
            // Định dạng Google trả về là "123s" hoặc "123.456s"
            String cleaned = durationStr.replace("s", "").trim();
            double val = Double.parseDouble(cleaned);
            return (int) Math.round(val);
        } catch (Exception e) {
            return 0;
        }
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
