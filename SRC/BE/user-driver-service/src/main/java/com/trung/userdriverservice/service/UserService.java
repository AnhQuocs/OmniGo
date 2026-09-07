package com.trung.userdriverservice.service;

import com.trung.userdriverservice.dto.request.PageRequestDTO;
import com.trung.userdriverservice.dto.request.UserRegisterRequest;
import com.trung.userdriverservice.dto.response.ApiResponse;
import com.trung.userdriverservice.dto.response.LoginResponse;
import com.trung.userdriverservice.dto.response.PageResponseDTO;
import com.trung.userdriverservice.dto.response.UserResponse;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.InvalidCredentialsException;
import com.trung.userdriverservice.exception.ResourceConflictException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;

import com.trung.userdriverservice.dto.request.UserLockRequest;

public interface UserService {
    ApiResponse<LoginResponse> registerCustomer(UserRegisterRequest request) throws ResourceConflictException, BadRequestException, InvalidCredentialsException;
    PageResponseDTO<UserResponse> getAllUsers(PageRequestDTO pageRequestDTO);
    ApiResponse<UserResponse> getUserById(Long id) throws ResourceNotFoundException;
    ApiResponse<UserResponse> toggleUserLock(Long userId, UserLockRequest request) throws ResourceNotFoundException;
    void lockUserAccount(Long userId) throws ResourceNotFoundException;
}
