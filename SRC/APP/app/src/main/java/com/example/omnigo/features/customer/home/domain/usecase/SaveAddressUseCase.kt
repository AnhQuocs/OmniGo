package com.example.omnigo.features.customer.home.domain.usecase

import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.repository.SavedAddressRepository
import javax.inject.Inject

class SaveAddressUseCase @Inject constructor(
    private val repository: SavedAddressRepository
) {
    suspend operator fun invoke(address: SavedAddress) {
        repository.saveAddress(address)
    }
}
