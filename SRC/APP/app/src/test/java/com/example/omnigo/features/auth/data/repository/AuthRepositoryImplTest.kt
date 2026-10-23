package com.example.omnigo.features.auth.data.repository

import app.cash.turbine.test
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.auth.data.remote.api.AuthApi
import com.example.omnigo.features.auth.data.remote.dto.response.UserResponse
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthRepositoryImplTest {

    private lateinit var authApi: AuthApi
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        authApi = mockk()
        firebaseAuth = mockk()
        sessionManager = mockk(relaxed = true)
        repository = AuthRepositoryImpl(authApi, firebaseAuth, sessionManager)
    }

    @Test
    fun `getMe should fetch user from API and update SessionManager on success`() = runTest {
        // Arrange
        val token = "mock_access_token"
        val userResponse = UserResponse(
            id = 1L,
            phoneNumber = "0839072300",
            fullName = "Bùi Anh Quốc",
            role = "CUSTOMER",
            status = "ACTIVE"
        )
        val apiResponse = ApiResponse(
            success = true,
            message = "Success",
            data = userResponse,
            timestamp = "2026-09-13T13:43:00"
        )

        coEvery { sessionManager.getAccessToken() } returns token
        coEvery { authApi.getMe() } returns Response.success(apiResponse)

        // Act
        repository.getMe().test {
            val result = awaitItem()
            
            // Assert
            assertEquals(userResponse.id, result.id)
            assertEquals(userResponse.fullName, result.fullName)
            
            coVerify {
                sessionManager.saveSession(
                    accessToken = token,
                    refreshToken = any(),
                    userId = userResponse.id,
                    phoneNumber = userResponse.phoneNumber!!,
                    fullName = userResponse.fullName!!,
                    role = userResponse.role!!
                )
            }
            awaitComplete()
        }
    }

    @Test
    fun `uploadDriverDocument returns url when API call succeeds`() = runTest {
        val requestBody = "fake image".toRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "cccd.jpg", requestBody)
        val uploadMap = mapOf("url" to "https://res.cloudinary.com/omnigo/image/upload/sample.jpg")
        val apiResponse = ApiResponse(
            success = true,
            message = "Uploaded",
            data = uploadMap,
            timestamp = "2026-10-22T17:00:00"
        )
        coEvery { authApi.uploadDriverDocument(part) } returns Response.success(apiResponse)

        val result = repository.uploadDriverDocument(part)

        assertTrue(result.isSuccess)
        assertEquals("https://res.cloudinary.com/omnigo/image/upload/sample.jpg", result.getOrNull())
    }

    @Test
    fun `uploadDriverDocument returns failure when API returns error`() = runTest {
        val requestBody = "fake image".toRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "cccd.jpg", requestBody)
        val errorResponseBody = "{\"success\":false,\"message\":\"File too large\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { authApi.uploadDriverDocument(part) } returns Response.error(400, errorResponseBody)

        val result = repository.uploadDriverDocument(part)

        assertTrue(result.isFailure)
    }
}
