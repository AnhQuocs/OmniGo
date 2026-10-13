package com.trung.fooddeliveryservice.service;

import com.trung.fooddeliveryservice.dto.response.RestaurantNearbyResponse;
import com.trung.fooddeliveryservice.repository.RestaurantRepository;
import com.trung.fooddeliveryservice.repository.projection.RestaurantNearbyProjection;
import com.trung.fooddeliveryservice.service.impl.RestaurantServiceImpl;
import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantNearbyServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    private RestaurantNearbyProjection createProjection(Long id, String name, double distance, RestaurantStatus status) {
        return new RestaurantNearbyProjection() {
            @Override public Long getId() { return id; }
            @Override public Long getOwnerId() { return 100L + id; }
            @Override public String getName() { return name; }
            @Override public String getPhone() { return "090000000" + id; }
            @Override public String getAddress() { return "Address " + id; }
            @Override public Double getLatitude() { return 21.0 + id * 0.01; }
            @Override public Double getLongitude() { return 105.8 + id * 0.01; }
            @Override public String getImageUrl() { return "https://example.com/img" + id + ".jpg"; }
            @Override public String getStatus() { return status != null ? status.name() : "OPEN"; }
            @Override public String getOpenTime() { return "08:00"; }
            @Override public String getCloseTime() { return "22:00"; }
            @Override public Double getRating() { return 4.8; }
            @Override public Integer getReviewCount() { return 15; }
            @Override public Double getDistanceKm() { return distance; }
        };
    }

    @Test
    @DisplayName("Trả về đúng tối đa 5 nhà hàng khi có nhiều hơn 5 nhà hàng gần nhất")
    void testNearbyRestaurants_whenMoreThanFive_returnsUpToFive() {
        List<RestaurantNearbyProjection> mockList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            mockList.add(createProjection((long) i, "Quán " + i, i * 1.2, RestaurantStatus.OPEN));
        }

        when(restaurantRepository.findNearbyRestaurants(21.03, 105.78, 5.0)).thenReturn(mockList);

        List<RestaurantNearbyResponse> results = restaurantService.getNearbyRestaurants(21.03, 105.78, 5.0);

        assertEquals(5, results.size());
        assertEquals("Quán 1", results.get(0).getName());
        assertEquals(1.2, results.get(0).getDistanceKm(), 0.001);
        assertEquals("Quán 5", results.get(4).getName());
        assertEquals(6.0, results.get(4).getDistanceKm(), 0.001);
    }

    @Test
    @DisplayName("Trả về đúng số lượng hiện có khi có dưới 5 nhà hàng")
    void testNearbyRestaurants_whenLessThanFive_returnsActualCount() {
        List<RestaurantNearbyProjection> mockList = List.of(
                createProjection(1L, "Quán A", 0.5, RestaurantStatus.OPEN),
                createProjection(2L, "Quán B", 2.3, RestaurantStatus.OPEN)
        );

        when(restaurantRepository.findNearbyRestaurants(21.0, 105.8, 5.0)).thenReturn(mockList);

        List<RestaurantNearbyResponse> results = restaurantService.getNearbyRestaurants(21.0, 105.8, 5.0);

        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());
    }

    @Test
    @DisplayName("Trả về danh sách rỗng khi không có nhà hàng phù hợp")
    void testNearbyRestaurants_whenEmpty_returnsEmptyList() {
        when(restaurantRepository.findNearbyRestaurants(0.0, 0.0, 5.0)).thenReturn(Collections.emptyList());

        List<RestaurantNearbyResponse> results = restaurantService.getNearbyRestaurants(0.0, 0.0, 5.0);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Giữ nguyên khoảng cách từ kết quả truy vấn")
    void testNearbyRestaurants_mapsRepositoryDistances() {
        List<RestaurantNearbyProjection> mockList = List.of(
                createProjection(10L, "Quán trong bán kính", 4.8, RestaurantStatus.OPEN)
        );

        when(restaurantRepository.findNearbyRestaurants(21.0, 105.8, 5.0)).thenReturn(mockList);

        List<RestaurantNearbyResponse> results = restaurantService.getNearbyRestaurants(21.0, 105.8, 5.0);

        assertEquals(1, results.size());
        assertEquals(4.8, results.get(0).getDistanceKm());
    }

    @Test
    @DisplayName("Tọa độ trùng nhau hoặc khoảng cách cực nhỏ cho distanceKm không bị NaN")
    void testNearbyRestaurants_identicalCoordinates_distanceNearZeroNotNaN() {
        List<RestaurantNearbyProjection> mockList = List.of(
                createProjection(1L, "Quán Ngay Điểm Nhận", 0.0, RestaurantStatus.OPEN)
        );

        when(restaurantRepository.findNearbyRestaurants(21.033, 105.789, 5.0)).thenReturn(mockList);

        List<RestaurantNearbyResponse> results = restaurantService.getNearbyRestaurants(21.033, 105.789, 5.0);

        assertEquals(1, results.size());
        assertFalse(Double.isNaN(results.get(0).getDistanceKm()));
        assertEquals(0.0, results.get(0).getDistanceKm(), 0.0001);
    }

    @Test
    @DisplayName("Khoảng cách bằng nhau giữ thứ tự ổn định theo ID")
    void testNearbyRestaurants_equalDistance_stableOrderById() {
        List<RestaurantNearbyProjection> mockList = List.of(
                createProjection(3L, "Quán ID 3", 1.5, RestaurantStatus.OPEN),
                createProjection(7L, "Quán ID 7", 1.5, RestaurantStatus.OPEN)
        );

        when(restaurantRepository.findNearbyRestaurants(21.0, 105.8, 5.0)).thenReturn(mockList);

        List<RestaurantNearbyResponse> results = restaurantService.getNearbyRestaurants(21.0, 105.8, 5.0);

        assertEquals(2, results.size());
        assertEquals(3L, results.get(0).getId());
        assertEquals(7L, results.get(1).getId());
    }

    @Test
    @DisplayName("Service kiểm tra thiếu tọa độ quăng BadRequestException")
    void testGetNearby_missingCoordinates_throwsBadRequest() {
        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants(null, "105.7891", null));

        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("21.0333", "", null));
    }

    @Test
    @DisplayName("Service kiểm tra tọa độ không phải số hoặc NaN/Infinity quăng BadRequestException")
    void testGetNearby_nonNumericOrNaN_throwsBadRequest() {
        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("abc", "105.7891", null));

        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("NaN", "105.7891", null));

        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("21.0333", "Infinity", null));
    }

    @Test
    @DisplayName("Service kiểm tra tọa độ vượt quá phạm vi [-90, 90], [-180, 180] quăng BadRequestException")
    void testGetNearby_outOfRangeCoordinates_throwsBadRequest() {
        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("91.5", "105.7891", null));

        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("-95.0", "105.7891", null));

        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("21.0333", "185.0", null));

        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("21.0333", "-181.0", null));
    }

    @Test
    @DisplayName("Service kiểm tra bán kính không hợp lệ quăng BadRequestException")
    void testGetNearby_invalidRadius_throwsBadRequest() {
        for (String radius : List.of("0", "-1", "NaN", "Infinity", "abc")) {
            assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                    restaurantService.getNearbyRestaurants("21", "105", radius));
        }
    }

    @Test
    @DisplayName("Service chấp nhận tham số String hợp lệ và gọi repository thành công")
    void testGetNearby_validStringParams_success() throws Exception {
        when(restaurantRepository.findNearbyRestaurants(eq(21.0333), eq(105.7891), eq(5.0), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<RestaurantNearbyResponse> results = restaurantService.getNearbyRestaurants("21.0333", "105.7891", null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Service kiểm tra page hoặc limit không hợp lệ quăng BadRequestException")
    void testGetNearby_invalidPageOrLimit_throwsBadRequest() {
        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("21", "105", "5.0", "-1", "10"));

        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("21", "105", "5.0", "abc", "10"));

        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("21", "105", "5.0", "0", "0"));

        assertThrows(com.trung.fooddeliveryservice.exception.BadRequestException.class, () ->
                restaurantService.getNearbyRestaurants("21", "105", "5.0", "0", "-5"));
    }

    @Test
    @DisplayName("Service phân trang thành công với tham số page và limit")
    void testGetNearby_validPageAndLimit_success() throws Exception {
        when(restaurantRepository.findNearbyRestaurants(eq(21.0333), eq(105.7891), eq(5.0), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<RestaurantNearbyResponse> results = restaurantService.getNearbyRestaurants(
                "21.0333", "105.7891", "5.0", "1", "5");

        assertNotNull(results);
        assertEquals(0, results.getTotalElements());
    }
}
