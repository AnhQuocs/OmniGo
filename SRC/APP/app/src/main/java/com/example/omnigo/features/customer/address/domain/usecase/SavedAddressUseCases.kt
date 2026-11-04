package com.example.omnigo.features.customer.address.domain.usecase

import com.example.omnigo.features.customer.address.domain.model.SavedAddress
import com.example.omnigo.features.customer.address.domain.repository.SavedAddressRepository
import javax.inject.Inject

class GetSavedAddressesUseCase @Inject constructor(
    private val repository: SavedAddressRepository
) {
    suspend operator fun invoke(): Result<List<SavedAddress>> {
        return repository.getSavedAddresses()
    }
}

class SaveAddressUseCase @Inject constructor(
    private val repository: SavedAddressRepository
) {
    suspend operator fun invoke(
        label: String,
        address: String,
        latitude: Double,
        longitude: Double,
        isDefault: Boolean = false
    ): Result<SavedAddress> {
        if (label.isBlank()) return Result.failure(IllegalArgumentException("Tên địa chỉ không được để trống"))
        if (address.isBlank()) return Result.failure(IllegalArgumentException("Địa chỉ không được để trống"))
        return repository.saveAddress(
            label = label,
            address = address,
            latitude = latitude,
            longitude = longitude,
            isDefault = isDefault
        )
    }
}

class UpdateSavedAddressUseCase @Inject constructor(
    private val repository: SavedAddressRepository
) {
    suspend operator fun invoke(
        addressId: Long,
        label: String,
        address: String,
        latitude: Double,
        longitude: Double,
        isDefault: Boolean = false
    ): Result<SavedAddress> {
        if (label.isBlank()) return Result.failure(IllegalArgumentException("Tên địa chỉ không được để trống"))
        if (address.isBlank()) return Result.failure(IllegalArgumentException("Địa chỉ không được để trống"))
        return repository.updateAddress(
            addressId = addressId,
            label = label,
            address = address,
            latitude = latitude,
            longitude = longitude,
            isDefault = isDefault
        )
    }
}

class DeleteAddressUseCase @Inject constructor(
    private val repository: SavedAddressRepository
) {
    suspend operator fun invoke(addressId: Long): Result<Unit> {
        return repository.deleteAddress(addressId)
    }
}
