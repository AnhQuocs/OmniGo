package com.trung.fooddeliveryservice.service;

import com.trung.fooddeliveryservice.dto.request.RestaurantLockRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantPartnerCreateRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantRequest;
import com.trung.fooddeliveryservice.dto.response.RestaurantNearbyResponse;
import com.trung.fooddeliveryservice.dto.response.RestaurantResponse;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RestaurantService {

    RestaurantResponse createRestaurant(RestaurantRequest request, Long ownerId) throws BadRequestException;

    RestaurantResponse registerPartner(RestaurantPartnerCreateRequest request) throws BadRequestException;

    RestaurantResponse getRestaurantById(Long id) throws ResourceNotFoundException;

    RestaurantResponse getRestaurantByOwnerId(Long ownerId) throws ResourceNotFoundException;

    List<RestaurantResponse> getAllOpenRestaurants();

    List<RestaurantResponse> searchRestaurants(String keyword);

    List<RestaurantNearbyResponse> getNearbyRestaurants(double latitude, double longitude, double radiusKm);

    List<RestaurantNearbyResponse> getNearbyRestaurants(String latStr, String lngStr, String radiusStr) throws BadRequestException;

    Page<RestaurantNearbyResponse> getNearbyRestaurants(double latitude, double longitude, double radiusKm, Pageable pageable);

    Page<RestaurantNearbyResponse> getNearbyRestaurants(String latStr, String lngStr, String radiusStr, String pageStr, String limitStr) throws BadRequestException;

    RestaurantResponse updateRestaurant(Long id, RestaurantRequest request, Long ownerId) throws ResourceNotFoundException, UnauthorizedException;

    RestaurantResponse toggleLockRestaurant(Long id, RestaurantLockRequest request) throws ResourceNotFoundException;

    RestaurantResponse approveRestaurant(Long id) throws ResourceNotFoundException;

    RestaurantResponse rejectRestaurant(Long id, String reason) throws ResourceNotFoundException;

    RestaurantResponse toggleRestaurantStatus(Long id, Long ownerId, RestaurantStatus status) throws ResourceNotFoundException, UnauthorizedException;
}
