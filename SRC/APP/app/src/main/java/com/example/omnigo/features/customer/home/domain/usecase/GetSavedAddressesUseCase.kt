package com.example.omnigo.features.customer.home.domain.usecase

import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.home.domain.repository.SavedAddressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedAddressesUseCase @Inject constructor(
    private val repository: SavedAddressRepository
) {
    operator fun invoke(): Flow<List<SavedAddress>> {
        return repository.getSavedAddresses()
    }
}
