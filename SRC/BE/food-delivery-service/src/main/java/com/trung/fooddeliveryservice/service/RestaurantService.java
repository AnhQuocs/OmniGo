package com.trung.fooddeliveryservice.service;

import com.trung.fooddeliveryservice.dto.request.RestaurantPartnerCreateRequest;
import com.trung.fooddeliveryservice.dto.request.RestaurantRequest;
import com.trung.fooddeliveryservice.dto.response.RestaurantResponse;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;

import java.util.List;

public interface RestaurantService {

    RestaurantResponse createRestaurant(RestaurantRequest request, Long ownerId) throws BadRequestException;

    RestaurantResponse registerPartner(RestaurantPartnerCreateRequest request) throws BadRequestException;

    RestaurantResponse getRestaurantById(Long id) throws ResourceNotFoundException;

    RestaurantResponse getRestaurantByOwnerId(Long ownerId) throws ResourceNotFoundException;

    List<RestaurantResponse> getAllOpenRestaurants();

    List<RestaurantResponse> searchRestaurants(String keyword);

    RestaurantResponse updateRestaurant(Long id, RestaurantRequest request, Long ownerId) throws ResourceNotFoundException, UnauthorizedException;

    RestaurantResponse toggleRestaurantStatus(Long id, Long ownerId, RestaurantStatus status) throws ResourceNotFoundException, UnauthorizedException;
}
