package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChangePasswordUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var changePasswordUseCase: ChangePasswordUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        changePasswordUseCase = ChangePasswordUseCase(authRepository)
    }

    @Test
    fun `invoke with short new password should fail without calling repository`() = runTest {
        val result = changePasswordUseCase(
            oldPassword = "oldPass123",
            newPassword = "123",
            confirmPassword = "123"
        )

        assertTrue(result.isFailure)
        assertEquals("Mật khẩu mới phải có ít nhất 6 ký tự", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.changePassword(any(), any(), any()) }
    }

    @Test
    fun `invoke with mismatched confirm password should fail without calling repository`() = runTest {
        val result = changePasswordUseCase(
            oldPassword = "oldPass123",
            newPassword = "newPassword123",
            confirmPassword = "differentPassword"
        )

        assertTrue(result.isFailure)
        assertEquals("Mật khẩu xác nhận không khớp", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.changePassword(any(), any(), any()) }
    }

    @Test
    fun `invoke with valid data should call repository and return success`() = runTest {
        coEvery {
            authRepository.changePassword("oldPass123", "newPassword123", "newPassword123")
        } returns Result.success("Đổi mật khẩu thành công")

        val result = changePasswordUseCase(
            oldPassword = "oldPass123",
            newPassword = "newPassword123",
            confirmPassword = "newPassword123"
        )

        assertTrue(result.isSuccess)
        assertEquals("Đổi mật khẩu thành công", result.getOrNull())
        coVerify(exactly = 1) {
            authRepository.changePassword("oldPass123", "newPassword123", "newPassword123")
        }
    }
}
