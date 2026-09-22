package com.example.omnigo.features.customer.home.domain.usecase

import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.repository.AdministrativeRegionRepository
import javax.inject.Inject

class GetProvincesUseCase @Inject constructor(
    private val regionRepository: AdministrativeRegionRepository
) {
    suspend operator fun invoke(query: String = ""): List<Province> {
        return if (query.isBlank()) {
            regionRepository.getProvinces()
        } else {
            regionRepository.searchProvinces(query)
        }
    }
}
