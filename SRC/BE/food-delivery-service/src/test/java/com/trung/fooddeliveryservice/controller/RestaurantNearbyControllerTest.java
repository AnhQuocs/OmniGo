package com.trung.fooddeliveryservice.controller;

import com.trung.fooddeliveryservice.dto.response.ApiResponse;
import com.trung.fooddeliveryservice.dto.response.RestaurantNearbyResponse;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.service.CloudinaryService;
import com.trung.fooddeliveryservice.service.RestaurantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantNearbyControllerTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private RestaurantController restaurantController;

    @Test
    @DisplayName("Controller chỉ gọi Service với phân trang limit/page và trả về kết quả từ Service với HTTP 200")
    void testGetNearby_delegatesToService() throws Exception {
        Page<RestaurantNearbyResponse> mockPage = new PageImpl<>(Collections.emptyList());
        when(restaurantService.getNearbyRestaurants("21.0333", "105.7891", "5.0", "0", "10"))
                .thenReturn(mockPage);

        ResponseEntity<ApiResponse<Page<RestaurantNearbyResponse>>> response =
                restaurantController.getNearbyRestaurants("21.0333", "105.7891", "5.0", "0", "10", null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        verify(restaurantService, times(1)).getNearbyRestaurants("21.0333", "105.7891", "5.0", "0", "10");
    }

    @Test
    @DisplayName("Khi Service quăng BadRequestException, Controller lan truyền ngoại lệ đúng chuẩn")
    void testGetNearby_serviceThrowsBadRequest_propagates() throws Exception {
        when(restaurantService.getNearbyRestaurants("abc", "105.7891", null, null, null))
                .thenThrow(new BadRequestException("Tọa độ latitude và longitude phải là số thực hợp lệ"));

        assertThrows(BadRequestException.class, () ->
                restaurantController.getNearbyRestaurants("abc", "105.7891", null, null, null, null));
    }
}
