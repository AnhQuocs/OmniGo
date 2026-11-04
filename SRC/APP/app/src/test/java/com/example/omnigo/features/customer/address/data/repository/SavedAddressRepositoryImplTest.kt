package com.example.omnigo.features.customer.address.data.repository

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.customer.address.data.remote.api.UserAddressApi
import com.example.omnigo.features.customer.address.data.remote.dto.SavedAddressResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class SavedAddressRepositoryImplTest {

    private lateinit var addressApi: UserAddressApi
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: SavedAddressRepositoryImpl

    @Before
    fun setUp() {
        addressApi = mockk()
        sessionManager = mockk()
        coEvery { sessionManager.getUserId() } returns 100L
        repository = SavedAddressRepositoryImpl(addressApi, sessionManager)
    }

    @Test
    fun `getSavedAddresses success should return mapped domain addresses`() = runTest {
        val dtos = listOf(
            SavedAddressResponseDto(
                id = 1L,
                userId = 100L,
                label = "Nhà riêng",
                addressType = "HOME",
                address = "123 Nguyễn Huệ, Q1",
                latitude = 10.77,
                longitude = 106.70,
                isDefault = true
            )
        )
        val apiResponse = ApiResponse(
            success = true,
            message = "Success",
            data = dtos,
            timestamp = "2026-11-02T00:00:00"
        )
        coEvery { addressApi.getSavedAddresses(100L) } returns Response.success(apiResponse)

        val result = repository.getSavedAddresses()

        assertTrue(result.isSuccess)
        val addresses = result.getOrNull()!!
        assertEquals(1, addresses.size)
        assertEquals("Nhà riêng", addresses[0].label)
        assertEquals("123 Nguyễn Huệ, Q1", addresses[0].address)
        assertTrue(addresses[0].isDefault)
    }

    @Test
    fun `getSavedAddresses failure with 400 error should extract error message`() = runTest {
        val errorJson = """{"success":false,"message":"Người dùng không tồn tại"}"""
        val responseBody = errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { addressApi.getSavedAddresses(100L) } returns Response.error(400, responseBody)

        val result = repository.getSavedAddresses()

        assertTrue(result.isFailure)
        assertEquals("Người dùng không tồn tại", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteAddress success should call api and return success`() = runTest {
        val apiResponse = ApiResponse<Unit>(
            success = true,
            message = "Deleted",
            data = Unit,
            timestamp = "2026-11-02T00:00:00"
        )
        coEvery { addressApi.deleteAddress(1L, 100L) } returns Response.success(apiResponse)

        val result = repository.deleteAddress(1L)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { addressApi.deleteAddress(1L, 100L) }
    }
}
