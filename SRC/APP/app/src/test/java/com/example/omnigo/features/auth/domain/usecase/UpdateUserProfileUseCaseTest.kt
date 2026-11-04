package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.model.UserRole
import com.example.omnigo.features.auth.domain.model.UserStatus
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class UpdateUserProfileUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: UpdateUserProfileUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = UpdateUserProfileUseCase(authRepository)
    }

    @Test
    fun `invoke with blank full name should return failure`() = runTest {
        val result = useCase("   ", "valid@email.com")

        assertTrue(result.isFailure)
        assertEquals("Họ và tên không được để trống", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.updateProfile(any(), any()) }
    }

    @Test
    fun `invoke with invalid email should return failure`() = runTest {
        val result = useCase("Nguyen Van A", "invalid-email")

        assertTrue(result.isFailure)
        assertEquals("Địa chỉ email không đúng định dạng", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.updateProfile(any(), any()) }
    }

    @Test
    fun `invoke with valid data should return success`() = runTest {
        val mockUser = AuthUser(
            id = 1L,
            phoneNumber = "0987654321",
            fullName = "Nguyen Van A",
            role = UserRole.CUSTOMER,
            status = UserStatus.ACTIVE,
            email = "valid@email.com",
            avatarUrl = null,
            driverInfo = null,
            createdAt = LocalDateTime.now()
        )
        coEvery { authRepository.updateProfile("Nguyen Van A", "valid@email.com") } returns Result.success(mockUser)

        val result = useCase("  Nguyen Van A  ", "valid@email.com")

        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
        coVerify(exactly = 1) { authRepository.updateProfile("Nguyen Van A", "valid@email.com") }
    }

    @Test
    fun `invoke with null or empty email should pass null to repository`() = runTest {
        val mockUser = AuthUser(
            id = 1L,
            phoneNumber = "0987654321",
            fullName = "Nguyen Van B",
            role = UserRole.CUSTOMER,
            status = UserStatus.ACTIVE,
            email = null,
            avatarUrl = null,
            driverInfo = null,
            createdAt = LocalDateTime.now()
        )
        coEvery { authRepository.updateProfile("Nguyen Van B", null) } returns Result.success(mockUser)

        val result = useCase("Nguyen Van B", "   ")

        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
        coVerify(exactly = 1) { authRepository.updateProfile("Nguyen Van B", null) }
    }
}
