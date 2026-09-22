package com.example.omnigo.core.region.data.mapper

import com.example.omnigo.core.region.data.dto.DistrictDto
import com.example.omnigo.core.region.data.dto.ProvinceDto
import com.example.omnigo.core.region.data.dto.WardDto
import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.model.Ward

fun ProvinceDto.toDomain(): Province? {
    val provinceId = id?.trim() ?: return null
    val provinceName = name?.trim() ?: return null
    if (provinceId.isEmpty() || provinceName.isEmpty()) return null

    return Province(
        id = provinceId,
        name = provinceName,
        districts = districts?.mapNotNull { it.toDomain() } ?: emptyList()
    )
}

fun DistrictDto.toDomain(): District? {
    val districtId = id?.trim() ?: return null
    val districtName = name?.trim() ?: return null
    if (districtId.isEmpty() || districtName.isEmpty()) return null

    return District(
        id = districtId,
        name = districtName,
        wards = wards?.mapNotNull { it.toDomain() } ?: emptyList()
    )
}

fun WardDto.toDomain(): Ward? {
    val wardId = id?.trim() ?: return null
    val wardName = name?.trim() ?: return null
    if (wardId.isEmpty() || wardName.isEmpty()) return null

    return Ward(
        id = wardId,
        name = wardName,
        level = level?.trim() ?: ""
    )
}

