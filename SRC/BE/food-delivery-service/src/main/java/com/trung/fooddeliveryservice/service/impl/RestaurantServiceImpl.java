package com.trung.fooddeliveryservice.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trung.fooddeliveryservice.dto.request.RestaurantLockRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantPartnerCreateRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantRequest;
import com.trung.fooddeliveryservice.dto.response.RestaurantNearbyResponse;
import com.trung.fooddeliveryservice.dto.response.RestaurantResponse;
import com.trung.fooddeliveryservice.entity.Restaurant;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.mapper.RestaurantMapper;
import com.trung.fooddeliveryservice.repository.FoodOrderReviewRepository;
import com.trung.fooddeliveryservice.repository.RestaurantRepository;
import com.trung.fooddeliveryservice.repository.projection.RestaurantNearbyProjection;
import com.trung.fooddeliveryservice.service.CloudinaryService;
import com.trung.fooddeliveryservice.service.RestaurantService;
import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final RestTemplate directRestTemplate;
    private final StringRedisTemplate redisTemplate;
    private final CloudinaryService cloudinaryService;
    @Lazy
    private final FoodOrderReviewRepository reviewRepository;

    @Value("${app.driver-service-url:http://localhost:8081}")
    private String driverServiceBaseUrl;

    @Override
    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request, Long ownerId) throws BadRequestException {
        if (restaurantRepository.findByOwnerId(ownerId).isPresent()) {
            log.warn("Chủ quán ID {} đã đăng ký nhà hàng trước đó", ownerId);
            throw new BadRequestException("Chủ quán đã đăng ký nhà hàng trên hệ thống");
        }

        Restaurant restaurant = restaurantMapper.toEntity(request, ownerId);
        if (restaurant.getImageUrl() != null && restaurant.getImageUrl().startsWith("data:image")) {
            restaurant.setImageUrl(cloudinaryService.uploadBase64(restaurant.getImageUrl()));
        }
        if (restaurant.getLicenseImageUrl() != null && restaurant.getLicenseImageUrl().startsWith("data:image")) {
            restaurant.setLicenseImageUrl(cloudinaryService.uploadBase64(restaurant.getLicenseImageUrl()));
        }
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Tạo thành công nhà hàng ID {} cho chủ quán ID {}", saved.getId(), ownerId);
        return restaurantMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RestaurantResponse registerPartner(RestaurantPartnerCreateRequest request) throws BadRequestException {
        log.info("Bắt đầu quy trình đăng ký đối tác nhà hàng '{}' - SĐT chủ: {}", request.getName(), request.getOwnerPhone());

        // 1. Tạo tài khoản User với Role RESTAURANT qua user-driver-service
        Long ownerId;
        try {
            Map<String, Object> userRequest = Map.of(
                    "phoneNumber", request.getOwnerPhone().trim(),
                    "email", StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : request.getOwnerPhone().trim() + "@partner.restaurant",
                    "password", request.getPassword(),
                    "fullName", request.getOwnerName().trim()
            );

            String userUrl = driverServiceBaseUrl + "/api/v1/internal/users/restaurant";
            ResponseEntity<Map> userResponse = directRestTemplate.postForEntity(userUrl, userRequest, Map.class);

            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                throw new BadRequestException("Không thể tạo tài khoản người dùng cho đối tác nhà hàng.");
            }

            Map<String, Object> body = userResponse.getBody();
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if (data == null || data.get("id") == null) {
                throw new BadRequestException("Dữ liệu tài khoản trả về không hợp lệ từ hệ thống người dùng.");
            }

            ownerId = Long.parseLong(data.get("id").toString());
        } catch (HttpStatusCodeException e) {
            log.error("Lỗi từ user-service khi tạo tài khoản nhà hàng: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            String errMessage = "Đăng ký tài khoản nhà hàng thất bại: ";
            try {
                if (e.getResponseBodyAsString().contains("message")) {
                    JsonNode node = new ObjectMapper().readTree(e.getResponseBodyAsString());
                    if (node.has("message")) {
                        errMessage += node.get("message").asText();
                    } else {
                        errMessage += e.getStatusCode().toString();
                    }
                } else {
                    errMessage += e.getStatusCode().toString();
                }
            } catch (Exception ignored) {
                errMessage += e.getMessage();
            }
            throw new BadRequestException(errMessage);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi kết nối tới user-service: {}", e.getMessage(), e);
            throw new BadRequestException("Lỗi kết nối tới dịch vụ quản lý người dùng: " + e.getMessage());
        }

        // 2. Kiểm tra xem chủ quán đã có nhà hàng chưa
        if (restaurantRepository.findByOwnerId(ownerId).isPresent()) {
            throw new BadRequestException("Chủ quán ID " + ownerId + " đã sở hữu một nhà hàng trên hệ thống.");
        }

        // 3. Xử lý ảnh nếu là Base64 -> chuyển qua Cloudinary
        String imageUrl = request.getImageUrl();
        if (imageUrl != null && imageUrl.startsWith("data:image")) {
            imageUrl = cloudinaryService.uploadBase64(imageUrl);
        }
        String licenseImageUrl = request.getLicenseImageUrl();
        if (licenseImageUrl != null && licenseImageUrl.startsWith("data:image")) {
            licenseImageUrl = cloudinaryService.uploadBase64(licenseImageUrl);
        }

        boolean isAutoApprove = Boolean.TRUE.equals(request.getAutoApprove());

        // 4. Tạo thông tin thực thể Restaurant
        Restaurant restaurant = Restaurant.builder()
                .ownerId(ownerId)
                .name(request.getName().trim())
                .phone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : request.getOwnerPhone().trim())
                .address(request.getAddress().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageUrl(imageUrl)
                .licenseImageUrl(licenseImageUrl)
                .openTime(StringUtils.hasText(request.getOpenTime()) ? request.getOpenTime().trim() : "08:00")
                .closeTime(StringUtils.hasText(request.getCloseTime()) ? request.getCloseTime().trim() : "22:00")
                .status(isAutoApprove ? RestaurantStatus.OPEN : RestaurantStatus.CLOSED)
                .isLocked(!isAutoApprove)
                .lockedReason(isAutoApprove ? null : "Chờ Quản trị viên duyệt hồ sơ đối tác")
                .rating(5.0)
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Tạo thành công nhà hàng đối tác ID {} cho chủ quán ID {} ({}) - Tự động duyệt: {}", saved.getId(), ownerId, request.getOwnerPhone(), isAutoApprove);
        return restaurantMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "restaurants", key = "#id")
    public RestaurantResponse getRestaurantById(Long id) throws ResourceNotFoundException {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy nhà hàng với ID {}", id);
                    return new ResourceNotFoundException("Không tìm thấy nhà hàng với ID: " + id);
                });
        syncRatingIfOutdated(restaurant);
        return restaurantMapper.toResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantByOwnerId(Long ownerId) throws ResourceNotFoundException {
        Restaurant restaurant = restaurantRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy nhà hàng của chủ quán ID {}", ownerId);
                    return new ResourceNotFoundException("Không tìm thấy nhà hàng của chủ quán ID: " + ownerId);
                });
        syncRatingIfOutdated(restaurant);
        return restaurantMapper.toResponse(restaurant);
    }

    @Override
    @Transactional
    public List<RestaurantResponse> getAllOpenRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        for (Restaurant r : restaurants) {
            syncRatingIfOutdated(r);
        }
        return restaurantMapper.toResponseList(restaurants);
    }

    @Override
    @Transactional
    public List<RestaurantResponse> searchRestaurants(String keyword) {
        List<Restaurant> restaurants = restaurantRepository.searchByNameOrAddress(keyword);
        for (Restaurant r : restaurants) {
            syncRatingIfOutdated(r);
        }
        return restaurantMapper.toResponseList(restaurants);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantNearbyResponse> getNearbyRestaurants(double latitude, double longitude, double radiusKm) {
        List<RestaurantNearbyProjection> projections =
                restaurantRepository.findNearbyRestaurants(latitude, longitude, radiusKm);

        if (projections == null || projections.isEmpty()) {
            return Collections.emptyList();
        }

        List<RestaurantNearbyResponse> result = new ArrayList<>();
        for (RestaurantNearbyProjection p : projections) {
            Double distance = p.getDistanceKm();
            if (distance == null || Double.isNaN(distance) || distance < 0) {
                distance = 0.0;
            }
            RestaurantStatus status = RestaurantStatus.OPEN;
            if (p.getStatus() != null) {
                try {
                    status = RestaurantStatus.valueOf(p.getStatus());
                } catch (Exception ignored) {
                }
            }

            result.add(RestaurantNearbyResponse.builder()
                    .id(p.getId())
                    .ownerId(p.getOwnerId())
                    .name(p.getName())
                    .phone(p.getPhone())
                    .address(p.getAddress())
                    .latitude(p.getLatitude())
                    .longitude(p.getLongitude())
                    .imageUrl(p.getImageUrl())
                    .status(status)
                    .openTime(p.getOpenTime())
                    .closeTime(p.getCloseTime())
                    .rating(p.getRating() != null ? p.getRating() : 5.0)
                    .reviewCount(p.getReviewCount() != null ? p.getReviewCount() : 0)
                    .distanceKm(distance)
                    .build());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantNearbyResponse> getNearbyRestaurants(double latitude, double longitude, double radiusKm, Pageable pageable) {
        Page<RestaurantNearbyProjection> projectionsPage =
                restaurantRepository.findNearbyRestaurants(latitude, longitude, radiusKm, pageable);

        List<RestaurantNearbyResponse> list = new ArrayList<>();
        for (RestaurantNearbyProjection p : projectionsPage.getContent()) {
            Double distance = p.getDistanceKm();
            if (distance == null || Double.isNaN(distance) || distance < 0) {
                distance = 0.0;
            }
            RestaurantStatus status = RestaurantStatus.OPEN;
            if (p.getStatus() != null) {
                try {
                    status = RestaurantStatus.valueOf(p.getStatus());
                } catch (Exception ignored) {
                }
            }

            list.add(RestaurantNearbyResponse.builder()
                    .id(p.getId())
                    .ownerId(p.getOwnerId())
                    .name(p.getName())
                    .phone(p.getPhone())
                    .address(p.getAddress())
                    .latitude(p.getLatitude())
                    .longitude(p.getLongitude())
                    .imageUrl(p.getImageUrl())
                    .status(status)
                    .openTime(p.getOpenTime())
                    .closeTime(p.getCloseTime())
                    .rating(p.getRating() != null ? p.getRating() : 5.0)
                    .reviewCount(p.getReviewCount() != null ? p.getReviewCount() : 0)
                    .distanceKm(distance)
                    .build());
        }

        return new PageImpl<>(list, pageable, projectionsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantNearbyResponse> getNearbyRestaurants(
            String latStr, String lngStr, String radiusStr, String pageStr, String limitStr) throws BadRequestException {
        if (!StringUtils.hasText(latStr) || !StringUtils.hasText(lngStr)) {
            throw new BadRequestException("Hai tham số latitude và longitude là bắt buộc");
        }

        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(latStr.trim());
            longitude = Double.parseDouble(lngStr.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Tọa độ latitude và longitude phải là số thực hợp lệ");
        }

        if (Double.isNaN(latitude) || Double.isInfinite(latitude) ||
                Double.isNaN(longitude) || Double.isInfinite(longitude)) {
            throw new BadRequestException("Tọa độ latitude và longitude phải là số thực hữu hạn");
        }

        if (latitude < -90.0 || latitude > 90.0) {
            throw new BadRequestException("Vĩ độ (latitude) phải nằm trong khoảng [-90, 90]");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new BadRequestException("Kinh độ (longitude) phải nằm trong khoảng [-180, 180]");
        }

        double radiusKm = 5.0;
        if (StringUtils.hasText(radiusStr)) {
            try {
                radiusKm = Double.parseDouble(radiusStr.trim());
            } catch (NumberFormatException e) {
                throw new BadRequestException("Bán kính radiusKm phải là số hợp lệ");
            }
        }
        if (!Double.isFinite(radiusKm) || radiusKm <= 0) {
            throw new BadRequestException("Bán kính radiusKm phải là số hữu hạn lớn hơn 0 km");
        }

        int page = 0;
        if (StringUtils.hasText(pageStr)) {
            try {
                page = Integer.parseInt(pageStr.trim());
                if (page < 0) {
                    throw new BadRequestException("Trang page không được âm");
                }
            } catch (NumberFormatException e) {
                throw new BadRequestException("Tham số page phải là số nguyên");
            }
        }

        int limit = 10;
        if (StringUtils.hasText(limitStr)) {
            try {
                limit = Integer.parseInt(limitStr.trim());
                if (limit <= 0) {
                    throw new BadRequestException("Tham số limit phải lớn hơn 0");
                }
            } catch (NumberFormatException e) {
                throw new BadRequestException("Tham số limit phải là số nguyên");
            }
        }

        Pageable pageable = PageRequest.of(page, limit);
        return getNearbyRestaurants(latitude, longitude, radiusKm, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantNearbyResponse> getNearbyRestaurants(String latStr, String lngStr, String radiusStr) throws BadRequestException {
        return getNearbyRestaurants(latStr, lngStr, radiusStr, "0", "100").getContent();
    }

    private void syncRatingIfOutdated(Restaurant r) {
        if (reviewRepository == null || r == null || r.getId() == null) return;
        try {
            long count = reviewRepository.countByRestaurantId(r.getId());
            if (count > 0 && (r.getReviewCount() == null || r.getReviewCount() != (int) count)) {
                Double avg = reviewRepository.calculateAverageRating(r.getId());
                r.setReviewCount((int) count);
                if (avg != null) {
                    r.setRating(Math.round(avg * 10.0) / 10.0);
                }
                restaurantRepository.save(r);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "restaurants", key = "#id")
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request, Long ownerId) throws ResourceNotFoundException, UnauthorizedException {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy nhà hàng để cập nhật với ID {}", id);
                    return new ResourceNotFoundException("Không tìm thấy nhà hàng với ID: " + id);
                });

        if (!restaurant.getOwnerId().equals(ownerId)) {
            log.warn("Người dùng ID {} không có quyền chỉnh sửa nhà hàng ID {}", ownerId, id);
            throw new UnauthorizedException("Bạn không có quyền chỉnh sửa thông tin nhà hàng này");
        }

        restaurant.setName(request.getName());
        restaurant.setPhone(request.getPhone());
        restaurant.setAddress(request.getAddress());
        restaurant.setLatitude(request.getLatitude());
        restaurant.setLongitude(request.getLongitude());
        String imageUrl = request.getImageUrl();
        if (imageUrl != null && imageUrl.startsWith("data:image")) {
            restaurant.setImageUrl(cloudinaryService.uploadBase64(imageUrl));
        } else {
            restaurant.setImageUrl(imageUrl);
        }

        if (request.getLicenseImageUrl() != null) {
            String licUrl = request.getLicenseImageUrl();
            if (licUrl.startsWith("data:image")) {
                restaurant.setLicenseImageUrl(cloudinaryService.uploadBase64(licUrl));
            } else {
                restaurant.setLicenseImageUrl(licUrl);
            }
        }
        restaurant.setOpenTime(request.getOpenTime());
        restaurant.setCloseTime(request.getCloseTime());

        Restaurant updated = restaurantRepository.save(restaurant);
        log.info("Cập nhật thành công thông tin nhà hàng ID {}", updated.getId());
        return restaurantMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "restaurants", key = "#id")
    public RestaurantResponse toggleLockRestaurant(Long id, RestaurantLockRequest request) throws ResourceNotFoundException {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy nhà hàng để khóa/mở khóa với ID {}", id);
                    return new ResourceNotFoundException("Không tìm thấy nhà hàng với ID: " + id);
                });

        boolean isLocked = Boolean.TRUE.equals(request.getIsLocked());
        restaurant.setIsLocked(isLocked);
        if (isLocked) {
            restaurant.setLockedReason(request.getReason() != null ? request.getReason().trim() : "Gian hàng bị khóa bởi Admin");
            restaurant.setLockedAt(LocalDateTime.now());
            restaurant.setStatus(RestaurantStatus.CLOSED);
        } else {
            restaurant.setLockedReason(null);
            restaurant.setLockedAt(null);
        }

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Cập nhật trạng thái khóa nhà hàng ID {} thành {}", saved.getId(), isLocked);

        // Đồng bộ khóa tài khoản người dùng chủ quán qua Redis & user-driver-service
        if (restaurant.getOwnerId() != null) {
            try {
                if (isLocked) {
                    redisTemplate.opsForValue().set("user_locked:" + restaurant.getOwnerId(), "true", Duration.ofDays(30));
                } else {
                    redisTemplate.delete("user_locked:" + restaurant.getOwnerId());
                }
            } catch (Exception e) {
                log.warn("Lỗi khi ghi cờ Redis user_locked cho ownerId {}: {}", restaurant.getOwnerId(), e.getMessage());
            }

            try {
                String lockUserUrl = driverServiceBaseUrl + "/api/v1/internal/users/" + restaurant.getOwnerId() + "/lock";
                Map<String, Object> lockPayload = Map.of(
                        "isLocked", isLocked,
                        "reason", isLocked ? (request.getReason() != null ? request.getReason().trim() : "Gian hàng bị khóa bởi Admin") : ""
                );
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(lockPayload, headers);
                directRestTemplate.postForEntity(lockUserUrl, entity, Map.class);
                log.info("Đã đồng bộ trạng thái khóa cho User chủ quán ID {} qua user-driver-service (POST)", restaurant.getOwnerId());
            } catch (Exception e) {
                log.warn("Lỗi khi đồng bộ khóa User chủ quán ID {} qua user-driver-service: {}", restaurant.getOwnerId(), e.getMessage());
            }
        }

        return restaurantMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "restaurants", key = "#id")
    public RestaurantResponse approveRestaurant(Long id) throws ResourceNotFoundException {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà hàng với ID: " + id));

        restaurant.setIsLocked(false);
        restaurant.setLockedReason(null);
        restaurant.setLockedAt(null);
        restaurant.setStatus(RestaurantStatus.OPEN);

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Admin đã phê duyệt mở gian hàng nhà hàng ID {}", saved.getId());

        // Mở khóa tài khoản chủ quán qua Redis & user-driver-service
        if (restaurant.getOwnerId() != null) {
            try {
                redisTemplate.delete("user_locked:" + restaurant.getOwnerId());
            } catch (Exception e) {
                log.warn("Lỗi khi xóa cờ Redis user_locked cho ownerId {}: {}", restaurant.getOwnerId(), e.getMessage());
            }

            try {
                String lockUserUrl = driverServiceBaseUrl + "/api/v1/internal/users/" + restaurant.getOwnerId() + "/lock";
                Map<String, Object> lockPayload = Map.of(
                        "isLocked", false,
                        "reason", ""
                );
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(lockPayload, headers);
                directRestTemplate.postForEntity(lockUserUrl, entity, Map.class);
            } catch (Exception e) {
                log.warn("Lỗi khi mở khóa User chủ quán ID {} qua user-driver-service: {}", restaurant.getOwnerId(), e.getMessage());
            }
        }

        return restaurantMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "restaurants", key = "#id")
    public RestaurantResponse rejectRestaurant(Long id, String reason) throws ResourceNotFoundException {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà hàng với ID: " + id));

        String rejectMsg = StringUtils.hasText(reason) ? "Từ chối hồ sơ đăng ký: " + reason.trim() : "Từ chối hồ sơ đăng ký nhà hàng";
        restaurant.setIsLocked(true);
        restaurant.setLockedReason(rejectMsg);
        restaurant.setLockedAt(LocalDateTime.now());
        restaurant.setStatus(RestaurantStatus.CLOSED);

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Admin đã từ chối hồ sơ nhà hàng ID {} với lý do: {}", saved.getId(), rejectMsg);

        if (restaurant.getOwnerId() != null) {
            try {
                redisTemplate.opsForValue().set("user_locked:" + restaurant.getOwnerId(), "true", Duration.ofDays(30));
            } catch (Exception e) {
                log.warn("Lỗi khi ghi cờ Redis user_locked cho ownerId {}: {}", restaurant.getOwnerId(), e.getMessage());
            }

            try {
                String lockUserUrl = driverServiceBaseUrl + "/api/v1/internal/users/" + restaurant.getOwnerId() + "/lock";
                Map<String, Object> lockPayload = Map.of(
                        "isLocked", true,
                        "reason", rejectMsg
                );
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(lockPayload, headers);
                directRestTemplate.postForEntity(lockUserUrl, entity, Map.class);
            } catch (Exception e) {
                log.warn("Lỗi khi khóa User chủ quán ID {} qua user-driver-service: {}", restaurant.getOwnerId(), e.getMessage());
            }
        }

        return restaurantMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "restaurants", key = "#id")
    public RestaurantResponse toggleRestaurantStatus(Long id, Long ownerId, RestaurantStatus status) throws ResourceNotFoundException, UnauthorizedException {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy nhà hàng để đổi trạng thái với ID {}", id);
                    return new ResourceNotFoundException("Không tìm thấy nhà hàng với ID: " + id);
                });

        if (!restaurant.getOwnerId().equals(ownerId)) {
            log.warn("Người dùng ID {} không có quyền đổi trạng thái nhà hàng ID {}", ownerId, id);
            throw new UnauthorizedException("Bạn không có quyền thay đổi trạng thái nhà hàng này");
        }

        restaurant.setStatus(status);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Cập nhật trạng thái nhà hàng ID {} thành {}", saved.getId(), status);
        return restaurantMapper.toResponse(saved);
    }
}
