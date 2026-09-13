package com.example.omnigo.features.auth.domain.usecase

import app.cash.turbine.test
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.model.UserRole
import com.example.omnigo.features.auth.domain.model.UserStatus
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class GetMeUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var getMeUseCase: GetMeUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        getMeUseCase = GetMeUseCase(authRepository)
    }

    @Test
    fun `invoke should return flow of AuthUser from repository`() = runTest {
        // Arrange
        val mockUser = AuthUser(
            id = 1L,
            phoneNumber = "0839072300",
            email = "test@example.com",
            fullName = "Bùi Anh Quốc",
            role = UserRole.CUSTOMER,
            status = UserStatus.ACTIVE,
            driverInfo = null,
            createdAt = LocalDateTime.now()
        )
        coEvery { authRepository.getMe() } returns flowOf(mockUser)

        // Act & Assert
        getMeUseCase().test {
            val result = awaitItem()
            assertEquals(mockUser, result)
            awaitComplete()
        }
    }
}
