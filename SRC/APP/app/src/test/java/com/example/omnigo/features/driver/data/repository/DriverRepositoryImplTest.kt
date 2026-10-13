package com.example.omnigo.features.driver.data.repository

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.network.dto.ApiResponse
import com.example.omnigo.features.driver.data.remote.api.DriverApi
import com.example.omnigo.features.driver.data.remote.dto.DriverAccountProfileResponse
import com.example.omnigo.features.driver.data.remote.dto.DriverLocationRequest
import com.example.omnigo.features.driver.data.remote.dto.DriverStatusUpdateRequest
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.model.DriverLocationUpdate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DriverRepositoryImplTest {

    private lateinit var driverApi: DriverApi
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: DriverRepositoryImpl

    @Before
    fun setUp() {
        driverApi = mockk()
        sessionManager = mockk()
        repository = DriverRepositoryImpl(driverApi, sessionManager)
    }

    @Test
    fun `updateDriverStatus with driverId calls updateDriverStatus on api`() = runTest {
        val request = DriverStatusUpdateRequest(status = "ONLINE", isOnline = true)
        val successResponse: ApiResponse<Any> = ApiResponse(
            success = true,
            message = "OK",
            data = "OK",
            timestamp = null
        )
        coEvery { driverApi.updateDriverStatus(101L, request) } returns successResponse

        val result = repository.updateDriverStatus(101L, true)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { driverApi.updateDriverStatus(101L, request) }
    }

    @Test
    fun `updateDriverStatus with null driverId queries sessionManager and falls back to updateMyDriverStatus`() = runTest {
        val request = DriverStatusUpdateRequest(status = "OFFLINE", isOnline = false)
        val successResponse: ApiResponse<Any> = ApiResponse(
            success = true,
            message = "OK",
            data = "OK",
            timestamp = null
        )
        coEvery { sessionManager.getUserId() } returns null
        coEvery { driverApi.updateMyDriverStatus(request) } returns successResponse

        val result = repository.updateDriverStatus(null, false)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { driverApi.updateMyDriverStatus(request) }
    }

    @Test
    fun `getDriverProfile parses response into DriverAccountProfile`() = runTest {
        val dto = DriverAccountProfileResponse(
            id = 101L,
            fullName = "Le Van Driver",
            phoneNumber = "0987654321",
            vehiclePlate = "51F-12345",
            status = "APPROVED",
            isOnline = true
        )
        val apiResponse = ApiResponse(
            success = true,
            message = "OK",
            data = dto,
            timestamp = null
        )
        coEvery { driverApi.getDriverProfileById(101L) } returns apiResponse

        val result = repository.getDriverProfile(101L)

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()!!
        assertEquals(101L, profile.driverId)
        assertEquals("Le Van Driver", profile.fullName)
        assertEquals(DriverApprovalStatus.APPROVED, profile.approvalStatus)
        assertEquals(true, profile.isOnline)
    }

    @Test
    fun `sendDriverLocation constructs request and calls api`() = runTest {
        val update = DriverLocationUpdate(
            latitude = 21.0285,
            longitude = 105.8542,
            bearing = 45f,
            speed = 10f,
            timestamp = 1690000000000L
        )
        val request = DriverLocationRequest(
            latitude = 21.0285,
            longitude = 105.8542,
            bearing = 45f,
            speed = 10f,
            timestamp = 1690000000000L
        )
        val apiResponse: ApiResponse<Any> = ApiResponse(
            success = true,
            message = "Location updated",
            data = "OK",
            timestamp = null
        )
        coEvery { driverApi.sendDriverLocation(request) } returns apiResponse

        val result = repository.sendDriverLocation(update)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { driverApi.sendDriverLocation(request) }
    }
}
