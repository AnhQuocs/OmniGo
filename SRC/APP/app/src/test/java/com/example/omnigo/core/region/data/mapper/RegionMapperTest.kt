package com.example.omnigo.core.region.data.mapper

import com.example.omnigo.core.region.data.dto.DistrictDto
import com.example.omnigo.core.region.data.dto.ProvinceDto
import com.example.omnigo.core.region.data.dto.WardDto
import org.junit.Assert.assertEquals
import org.junit.Test

class RegionMapperTest {

    @Test
    fun `toDomain with full ProvinceDto converts correctly`() {
        val dto = ProvinceDto(
            id = "01",
            name = "Thành phố Hà Nội",
            districts = listOf(
                DistrictDto(
                    id = "001",
                    name = "Quận Ba Đình",
                    wards = listOf(
                        WardDto(id = "00001", name = "Phường Phúc Xá", level = "Phường")
                    )
                )
            )
        )

        val domain = dto.toDomain()

        org.junit.Assert.assertNotNull(domain)
        assertEquals("01", domain?.id)
        assertEquals("Thành phố Hà Nội", domain?.name)
        assertEquals(1, domain?.districts?.size)
        assertEquals("001", domain?.districts?.get(0)?.id)
        assertEquals("Quận Ba Đình", domain?.districts?.get(0)?.name)
        assertEquals(1, domain?.districts?.get(0)?.wards?.size)
        assertEquals("00001", domain?.districts?.get(0)?.wards?.get(0)?.id)
        assertEquals("Phường Phúc Xá", domain?.districts?.get(0)?.wards?.get(0)?.name)
        assertEquals("Phường", domain?.districts?.get(0)?.wards?.get(0)?.level)
    }

    @Test
    fun `toDomain with null sub-lists provides empty list defaults`() {
        val dto = ProvinceDto(
            id = "02",
            name = "Tỉnh Hà Giang",
            districts = null
        )

        val domain = dto.toDomain()

        org.junit.Assert.assertNotNull(domain)
        assertEquals("02", domain?.id)
        assertEquals("Tỉnh Hà Giang", domain?.name)
        assertEquals(0, domain?.districts?.size)
    }

    @Test
    fun `toDomain with null or blank id returns null`() {
        val dto = ProvinceDto(id = null, name = "Invalid")
        val domain = dto.toDomain()
        org.junit.Assert.assertNull(domain)
    }
}
