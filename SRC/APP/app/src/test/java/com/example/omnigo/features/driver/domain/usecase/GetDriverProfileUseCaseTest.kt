package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDriverProfileUseCaseTest {

    private lateinit var repository: DriverRepository
    private lateinit var useCase: GetDriverProfileUseCase

    private val mockProfile = DriverAccountProfile(
        driverId = 101L,
        fullName = "Nguyen Van Driver",
        phoneNumber = "0912345678",
        vehiclePlate = "29A-12345",
        approvalStatus = DriverApprovalStatus.APPROVED,
        isOnline = true,
        avatarUrl = null
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetDriverProfileUseCase(repository)
    }

    @Test
    fun `invoke with driverId returns driver profile successfully`() = runTest {
        coEvery { repository.getDriverProfile(101L) } returns Result.success(mockProfile)

        val result = useCase(101L)

        assertTrue(result.isSuccess)
        assertEquals(mockProfile, result.getOrNull())
        coVerify(exactly = 1) { repository.getDriverProfile(101L) }
    }

    @Test
    fun `invoke with null driverId queries current driver profile`() = runTest {
        coEvery { repository.getDriverProfile(null) } returns Result.success(mockProfile)

        val result = useCase(null)

        assertTrue(result.isSuccess)
        assertEquals(mockProfile, result.getOrNull())
        coVerify(exactly = 1) { repository.getDriverProfile(null) }
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val error = RuntimeException("Driver not found")
        coEvery { repository.getDriverProfile(999L) } returns Result.failure(error)

        val result = useCase(999L)

        assertTrue(result.isFailure)
        assertEquals("Driver not found", result.exceptionOrNull()?.message)
    }
}
