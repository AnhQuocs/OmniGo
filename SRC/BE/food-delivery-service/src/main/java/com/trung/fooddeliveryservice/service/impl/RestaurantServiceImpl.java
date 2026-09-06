package com.trung.fooddeliveryservice.service.impl;

import com.trung.fooddeliveryservice.dto.request.RestaurantPartnerCreateRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantRequest;
import com.trung.fooddeliveryservice.dto.response.RestaurantResponse;
import com.trung.fooddeliveryservice.entity.Restaurant;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.mapper.RestaurantMapper;
import com.trung.fooddeliveryservice.repository.RestaurantRepository;
import com.trung.fooddeliveryservice.service.RestaurantService;
import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final RestTemplate directRestTemplate;

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
                    com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(e.getResponseBodyAsString());
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

        // 3. Tạo thông tin thực thể Restaurant
        Restaurant restaurant = Restaurant.builder()
                .ownerId(ownerId)
                .name(request.getName().trim())
                .phone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : request.getOwnerPhone().trim())
                .address(request.getAddress().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageUrl(request.getImageUrl())
                .openTime(StringUtils.hasText(request.getOpenTime()) ? request.getOpenTime().trim() : "08:00")
                .closeTime(StringUtils.hasText(request.getCloseTime()) ? request.getCloseTime().trim() : "22:00")
                .status(RestaurantStatus.OPEN)
                .rating(5.0)
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Tạo thành công nhà hàng đối tác ID {} cho chủ quán ID {} ({})", saved.getId(), ownerId, request.getOwnerPhone());
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
        return restaurantMapper.toResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllOpenRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurantMapper.toResponseList(restaurants);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchRestaurants(String keyword) {
        List<Restaurant> restaurants = restaurantRepository.searchByNameOrAddress(keyword);
        return restaurantMapper.toResponseList(restaurants);
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
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setOpenTime(request.getOpenTime());
        restaurant.setCloseTime(request.getCloseTime());

        Restaurant updated = restaurantRepository.save(restaurant);
        log.info("Cập nhật thành công thông tin nhà hàng ID {}", updated.getId());
        return restaurantMapper.toResponse(updated);
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
