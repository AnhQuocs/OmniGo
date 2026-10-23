package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UploadDriverDocumentUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: UploadDriverDocumentUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = UploadDriverDocumentUseCase(authRepository)
    }

    @Test
    fun `invoke calls repository and returns success url when upload succeeds`() = runTest {
        val requestBody = "fake image content".toRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "cccd_front.jpg", requestBody)
        val expectedUrl = "https://res.cloudinary.com/omnigo/image/upload/v12345/cccd_front.jpg"

        coEvery { authRepository.uploadDriverDocument(part) } returns Result.success(expectedUrl)

        val result = useCase(part)

        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrNull())
        coVerify(exactly = 1) { authRepository.uploadDriverDocument(part) }
    }

    @Test
    fun `invoke calls repository and returns failure when upload fails`() = runTest {
        val requestBody = "fake image content".toRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "cccd_front.jpg", requestBody)
        val exception = RuntimeException("Upload to Cloudinary failed")

        coEvery { authRepository.uploadDriverDocument(part) } returns Result.failure(exception)

        val result = useCase(part)

        assertTrue(result.isFailure)
        assertEquals("Upload to Cloudinary failed", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { authRepository.uploadDriverDocument(part) }
    }
}
