package com.example.omnigo.core.network

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import dagger.Lazy
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TokenAuthenticatorTest {

    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var authRepositoryProvider: Lazy<AuthRepository>
    private lateinit var authenticator: TokenAuthenticator

    @Before
    fun setUp() {
        sessionManager = mockk()
        authRepository = mockk()
        authRepositoryProvider = mockk()
        every { authRepositoryProvider.get() } returns authRepository
        authenticator = TokenAuthenticator(sessionManager, authRepositoryProvider)
    }

    @Test
    fun `authenticate returns null if response code is not 401`() = runTest {
        val request = Request.Builder().url("http://localhost/").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(403) // Forbidden
            .message("Forbidden")
            .build()

        val result = authenticator.authenticate(null, response)

        assertNull(result)
    }

    @Test
    fun `authenticate returns new request if token was already refreshed by another thread`() = runTest {
        val oldToken = "old_token"
        val newToken = "new_token"
        
        val request = Request.Builder()
            .url("http://localhost/")
            .header("Authorization", "Bearer $oldToken")
            .build()
        
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        coEvery { sessionManager.getAccessToken() } returns newToken

        val result = authenticator.authenticate(null, response)

        assertEquals("Bearer $newToken", result?.header("Authorization"))
    }

    @Test
    fun `authenticate calls refreshToken and returns new request on success`() = runTest {
        val oldToken = "old_token"
        val newToken = "new_token"
        
        val request = Request.Builder()
            .url("http://localhost/")
            .header("Authorization", "Bearer $oldToken")
            .build()
        
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        coEvery { sessionManager.getAccessToken() } returnsMany listOf(oldToken, newToken)
        coEvery { authRepository.refreshToken() } returns true

        val result = authenticator.authenticate(null, response)

        assertEquals("Bearer $newToken", result?.header("Authorization"))
    }

    @Test
    fun `authenticate returns null if refreshToken fails`() = runTest {
        val token = "some_token"
        
        val request = Request.Builder()
            .url("http://localhost/")
            .header("Authorization", "Bearer $token")
            .build()
        
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        coEvery { sessionManager.getAccessToken() } returns token
        coEvery { authRepository.refreshToken() } returns false

        val result = authenticator.authenticate(null, response)

        assertNull(result)
    }
}
