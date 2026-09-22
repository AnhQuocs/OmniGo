package com.example.omnigo.features.customer.home.domain.usecase

import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.repository.AdministrativeRegionRepository
import javax.inject.Inject

class GetDistrictsByProvinceUseCase @Inject constructor(
    private val regionRepository: AdministrativeRegionRepository
) {
    suspend operator fun invoke(provinceId: String, query: String = ""): List<District> {
        return if (query.isBlank()) {
            regionRepository.getDistrictsByProvinceId(provinceId)
        } else {
            regionRepository.searchDistricts(provinceId, query)
        }
    }
}
