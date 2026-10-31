package com.trung.userdriverservice.service;

import com.trung.userdriverservice.dto.request.SaveAddressRequest;
import com.trung.userdriverservice.dto.response.SavedAddressResponse;
import com.trung.userdriverservice.exception.ResourceNotFoundException;

import java.util.List;

public interface UserSavedAddressService {
    List<SavedAddressResponse> getSavedAddresses(Long userId);
    SavedAddressResponse saveAddress(Long userId, SaveAddressRequest request);
    SavedAddressResponse updateAddress(Long userId, Long addressId, SaveAddressRequest request) throws ResourceNotFoundException;
    void deleteAddress(Long userId, Long addressId) throws ResourceNotFoundException;
    SavedAddressResponse setDefaultAddress(Long userId, Long addressId) throws ResourceNotFoundException;
}

