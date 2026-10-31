package com.trung.userdriverservice.service.impl;

import com.trung.userdriverservice.dto.request.SaveAddressRequest;
import com.trung.userdriverservice.dto.response.SavedAddressResponse;
import com.trung.userdriverservice.entity.UserSavedAddress;
import com.trung.userdriverservice.exception.ResourceNotFoundException;
import com.trung.userdriverservice.repository.UserSavedAddressRepository;
import com.trung.userdriverservice.service.UserSavedAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSavedAddressServiceImpl implements UserSavedAddressService {

    private final UserSavedAddressRepository addressRepository;

    @Override
    public List<SavedAddressResponse> getSavedAddresses(Long userId) {
        List<UserSavedAddress> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        return addresses.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SavedAddressResponse saveAddress(Long userId, SaveAddressRequest request) {
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.resetDefaultAddressForUser(userId);
        }

        UserSavedAddress entity = UserSavedAddress.builder()
                .userId(userId)
                .label(request.getLabel())
                .addressType(request.getAddressType() != null ? request.getAddressType().toUpperCase() : "OTHER")
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();

        UserSavedAddress saved = addressRepository.save(entity);
        log.info("Đã lưu địa chỉ mới ID {} cho User ID {}", saved.getId(), userId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SavedAddressResponse updateAddress(Long userId, Long addressId, SaveAddressRequest request) throws ResourceNotFoundException {
        UserSavedAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ đã lưu với ID: " + addressId));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.resetDefaultAddressForUser(userId);
            address.setIsDefault(true);
        } else if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        address.setLabel(request.getLabel());
        if (request.getAddressType() != null && !request.getAddressType().isBlank()) {
            address.setAddressType(request.getAddressType().toUpperCase());
        }
        address.setAddress(request.getAddress());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());

        UserSavedAddress updated = addressRepository.save(address);
        log.info("Đã cập nhật địa chỉ ID {} cho User ID {}", updated.getId(), userId);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) throws ResourceNotFoundException {
        UserSavedAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ đã lưu với ID: " + addressId));
        addressRepository.delete(address);
        log.info("Đã xóa địa chỉ ID {} của User ID {}", addressId, userId);
    }

    @Override
    @Transactional
    public SavedAddressResponse setDefaultAddress(Long userId, Long addressId) throws ResourceNotFoundException {
        UserSavedAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ đã lưu với ID: " + addressId));

        addressRepository.resetDefaultAddressForUser(userId);
        address.setIsDefault(true);
        UserSavedAddress updated = addressRepository.save(address);
        log.info("Đã đặt địa chỉ ID {} làm mặc định cho User ID {}", addressId, userId);
        return toResponse(updated);
    }

    private SavedAddressResponse toResponse(UserSavedAddress a) {
        return SavedAddressResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .label(a.getLabel())
                .addressType(a.getAddressType())
                .address(a.getAddress())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .isDefault(a.getIsDefault())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}

