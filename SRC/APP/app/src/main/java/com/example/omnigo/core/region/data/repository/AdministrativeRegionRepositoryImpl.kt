package com.example.omnigo.core.region.data.repository

import android.content.Context
import com.example.omnigo.core.region.data.dto.ProvinceDto
import com.example.omnigo.core.region.data.mapper.toDomain
import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.model.Ward
import com.example.omnigo.core.region.domain.repository.AdministrativeRegionRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.text.Normalizer
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdministrativeRegionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : AdministrativeRegionRepository {

    private var cachedProvinces: List<Province>? = null
    private val mutex = Mutex()

    private suspend fun ensureLoaded(): List<Province> {
        cachedProvinces?.let { return it }
        return mutex.withLock {
            cachedProvinces?.let { return@withLock it }
            withContext(Dispatchers.IO) {
                try {
                    context.assets.open("vietnam_regions.json").use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            val listType = object : TypeToken<List<ProvinceDto>>() {}.type
                            val dtos: List<ProvinceDto> = gson.fromJson(reader, listType)
                            val domainList = dtos.mapNotNull { it.toDomain() }
                            cachedProvinces = domainList
                            domainList
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AdminRegionRepo", "Failed to load vietnam_regions.json", e)
                    emptyList()
                }
            }
        }
    }

    override suspend fun getProvinces(): List<Province> {
        return ensureLoaded()
    }

    override suspend fun getDistrictsByProvinceId(provinceId: String): List<District> {
        val provinces = ensureLoaded()
        val province = provinces.find { it.id == provinceId }
        return province?.districts ?: emptyList()
    }

    override suspend fun getWardsByDistrictId(provinceId: String, districtId: String): List<Ward> {
        val districts = getDistrictsByProvinceId(provinceId)
        val district = districts.find { it.id == districtId }
        return district?.wards ?: emptyList()
    }

    override suspend fun searchProvinces(query: String): List<Province> {
        val provinces = ensureLoaded()
        if (query.isBlank()) return provinces
        val normalizedQuery = removeAccents(query.trim().lowercase())
        return provinces.filter {
            removeAccents(it.name.lowercase()).contains(normalizedQuery)
        }
    }

    override suspend fun searchDistricts(provinceId: String, query: String): List<District> {
        val districts = getDistrictsByProvinceId(provinceId)
        if (query.isBlank()) return districts
        val normalizedQuery = removeAccents(query.trim().lowercase())
        return districts.filter {
            removeAccents(it.name.lowercase()).contains(normalizedQuery)
        }
    }

    override suspend fun searchWards(provinceId: String, districtId: String, query: String): List<Ward> {
        val wards = getWardsByDistrictId(provinceId, districtId)
        if (query.isBlank()) return wards
        val normalizedQuery = removeAccents(query.trim().lowercase())
        return wards.filter {
            removeAccents(it.name.lowercase()).contains(normalizedQuery)
        }
    }

    private fun removeAccents(input: String): String {
        val nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString)
            .replaceAll("")
            .replace('đ', 'd')
            .replace('Đ', 'D')
    }
}
