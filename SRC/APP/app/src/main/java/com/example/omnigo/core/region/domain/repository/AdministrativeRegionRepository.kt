package com.example.omnigo.core.region.domain.repository

import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.model.Ward

interface AdministrativeRegionRepository {
    suspend fun getProvinces(): List<Province>
    suspend fun getDistrictsByProvinceId(provinceId: String): List<District>
    suspend fun getWardsByDistrictId(provinceId: String, districtId: String): List<Ward>
    suspend fun searchProvinces(query: String): List<Province>
    suspend fun searchDistricts(provinceId: String, query: String): List<District>
    suspend fun searchWards(provinceId: String, districtId: String, query: String): List<Ward>
}
