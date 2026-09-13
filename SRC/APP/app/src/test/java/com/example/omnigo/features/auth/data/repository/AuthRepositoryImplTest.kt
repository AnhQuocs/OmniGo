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
import org.junit.Assert.assertEquals
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
}
