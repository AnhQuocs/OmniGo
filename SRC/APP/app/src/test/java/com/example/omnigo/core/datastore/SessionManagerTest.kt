package com.example.omnigo.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.omnigo.core.security.CryptoManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SessionManagerTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var cryptoManager: CryptoManager
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        dataStore = mockk(relaxed = true)
        cryptoManager = mockk()
        sessionManager = SessionManager(dataStore, cryptoManager)
    }

    @Test
    fun `saveSession should encrypt and save tokens on success`() = runTest {
        // Arrange
        val accessToken = "raw_access"
        val encryptedAccess = "enc_access"
        every { cryptoManager.encrypt(accessToken) } returns Result.success(encryptedAccess)
        
        val mockMutablePreferences = mockk<MutablePreferences>(relaxed = true)
        coEvery { dataStore.edit(any()) } coAnswers {
            val transform = firstArg<(MutablePreferences) -> Unit>()
            transform(mockMutablePreferences)
            mockMutablePreferences
        }

        // Act
        sessionManager.saveSession(accessToken = accessToken, refreshToken = null)

        // Assert
        coVerify { mockMutablePreferences[any<Preferences.Key<String>>()] = encryptedAccess }
    }

    @Test
    fun `saveSession should remove token if encryption fails`() = runTest {
        // Arrange
        val accessToken = "raw_access"
        every { cryptoManager.encrypt(accessToken) } returns Result.failure(Exception("Fail"))
        
        val mockMutablePreferences = mockk<MutablePreferences>(relaxed = true)
        coEvery { dataStore.edit(any()) } coAnswers {
            val transform = firstArg<(MutablePreferences) -> Unit>()
            transform(mockMutablePreferences)
            mockMutablePreferences
        }

        // Act
        sessionManager.saveSession(accessToken = accessToken, refreshToken = null)

        // Assert
        coVerify { mockMutablePreferences.remove(any()) }
    }

    @Test
    fun `getAccessToken should return decrypted token on success`() = runTest {
        // Arrange
        val encrypted = "enc_access"
        val raw = "raw_access"
        val mockPreferences = mockk<Preferences>()
        every { mockPreferences[any<Preferences.Key<String>>()] } returns encrypted
        coEvery { dataStore.data } returns flowOf(mockPreferences)
        every { cryptoManager.decrypt(encrypted) } returns Result.success(raw)

        // Act
        val result = sessionManager.getAccessToken()

        // Assert
        assertEquals(raw, result)
    }

    @Test
    fun `getAccessToken should clear session and return null on decryption failure`() = runTest {
        // Arrange
        val encrypted = "invalid"
        val mockPreferences = mockk<Preferences>()
        every { mockPreferences[any<Preferences.Key<String>>()] } returns encrypted
        coEvery { dataStore.data } returns flowOf(mockPreferences)
        every { cryptoManager.decrypt(encrypted) } returns Result.failure(Exception("Decryption error"))

        // Act
        val result = sessionManager.getAccessToken()

        // Assert
        assertNull(result)
        coVerify { dataStore.edit(any()) } // clearSession()
    }
}
