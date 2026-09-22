package com.example.omnigo.features.customer.home.domain.usecase

import com.example.omnigo.core.region.domain.model.Ward
import com.example.omnigo.core.region.domain.repository.AdministrativeRegionRepository
import javax.inject.Inject

class GetWardsByDistrictUseCase @Inject constructor(
    private val regionRepository: AdministrativeRegionRepository
) {
    suspend operator fun invoke(provinceId: String, districtId: String, query: String = ""): List<Ward> {
        return if (query.isBlank()) {
            regionRepository.getWardsByDistrictId(provinceId, districtId)
        } else {
            regionRepository.searchWards(provinceId, districtId, query)
        }
    }
}
