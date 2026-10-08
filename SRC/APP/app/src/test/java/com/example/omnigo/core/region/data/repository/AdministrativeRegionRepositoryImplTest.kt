package com.example.omnigo.core.region.data.repository

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class AdministrativeRegionRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var repository: AdministrativeRegionRepositoryImpl

    private val sampleJson = """
        [
          {
            "Id": "01",
            "Name": "Thành phố Hà Nội",
            "Districts": [
              {
                "Id": "001",
                "Name": "Quận Ba Đình",
                "Wards": [
                  { "Id": "00001", "Name": "Phường Phúc Xá", "Level": "Phường" },
                  { "Id": "00004", "Name": "Phường Trúc Bạch", "Level": "Phường" }
                ]
              },
              {
                "Id": "005",
                "Name": "Quận Cầu Giấy",
                "Wards": [
                  { "Id": "00157", "Name": "Phường Nghĩa Đô", "Level": "Phường" }
                ]
              }
            ]
          },
          {
            "Id": "79",
            "Name": "Thành phố Hồ Chí Minh",
            "Districts": [
              {
                "Id": "760",
                "Name": "Quận 1",
                "Wards": [
                  { "Id": "26734", "Name": "Phường Bến Nghé", "Level": "Phường" }
                ]
              }
            ]
          }
        ]
    """.trimIndent()

    @Before
    fun setUp() {
        context = mockk()
        assetManager = mockk()
        every { context.assets } returns assetManager
        every { assetManager.open("vietnam_regions.json") } answers {
            ByteArrayInputStream(sampleJson.toByteArray())
        }

        repository = AdministrativeRegionRepositoryImpl(context, Gson())
    }

    @Test
    fun `getProvinces loads and returns all provinces`() = runTest {
        val provinces = repository.getProvinces()
        assertEquals(2, provinces.size)
        assertEquals("Thành phố Hà Nội", provinces[0].name)
        assertEquals("Thành phố Hồ Chí Minh", provinces[1].name)
    }

    @Test
    fun `getDistrictsByProvinceId returns correct districts for matching province`() = runTest {
        val districts = repository.getDistrictsByProvinceId("01")
        assertEquals(2, districts.size)
        assertEquals("Quận Ba Đình", districts[0].name)
        assertEquals("Quận Cầu Giấy", districts[1].name)
    }

    @Test
    fun `getWardsByDistrictId returns correct wards`() = runTest {
        val wards = repository.getWardsByDistrictId("01", "001")
        assertEquals(2, wards.size)
        assertEquals("Phường Phúc Xá", wards[0].name)
        assertEquals("Phường Trúc Bạch", wards[1].name)
    }

    @Test
    fun `searchProvinces with accent-insensitive query returns matching provinces`() = runTest {
        val results = repository.searchProvinces("ha noi")
        assertEquals(1, results.size)
        assertEquals("Thành phố Hà Nội", results[0].name)
    }

    @Test
    fun `searchDistricts with matching query filters properly`() = runTest {
        val results = repository.searchDistricts("01", "cau giay")
        assertEquals(1, results.size)
        assertEquals("Quận Cầu Giấy", results[0].name)
    }

    @Test
    fun `searchWards with non-matching query returns empty list`() = runTest {
        val results = repository.searchWards("01", "001", "NonExistent")
        assertTrue(results.isEmpty())
    }
}
