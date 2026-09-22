package com.example.omnigo.features.customer.home.domain.usecase

import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.repository.AdministrativeRegionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetProvincesUseCaseTest {

    private lateinit var regionRepository: AdministrativeRegionRepository
    private lateinit var useCase: GetProvincesUseCase

    @Before
    fun setUp() {
        regionRepository = mockk()
        useCase = GetProvincesUseCase(regionRepository)
    }

    @Test
    fun `invoke with blank query calls getProvinces`() = runTest {
        val mockProvinces = listOf(
            Province("01", "Hà Nội"),
            Province("79", "Hồ Chí Minh")
        )
        coEvery { regionRepository.getProvinces() } returns mockProvinces

        val result = useCase("")

        assertEquals(2, result.size)
        assertEquals("Hà Nội", result[0].name)
    }

    @Test
    fun `invoke with search query calls searchProvinces`() = runTest {
        val mockProvinces = listOf(Province("01", "Hà Nội"))
        coEvery { regionRepository.searchProvinces("Ha") } returns mockProvinces

        val result = useCase("Ha")

        assertEquals(1, result.size)
        assertEquals("Hà Nội", result[0].name)
    }
}
