package com.example.omnigo.features.driver.data.mapper

import com.example.omnigo.features.driver.data.remote.dto.DriverAccountProfileResponse
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverMapperTest {

    @Test
    fun `mapToDomain with full data maps all fields correctly`() {
        val dto = DriverAccountProfileResponse(
            id = 55L,
            fullName = "Pham Van Driver",
            phoneNumber = "0901234567",
            vehiclePlate = "29-X1 999.99",
            status = "APPROVED",
            isOnline = true,
            avatarUrl = "https://example.com/avatar.jpg"
        )

        val domain = DriverMapper.mapToDomain(dto)

        assertEquals(55L, domain.driverId)
        assertEquals("Pham Van Driver", domain.fullName)
        assertEquals("0901234567", domain.phoneNumber)
        assertEquals("29-X1 999.99", domain.vehiclePlate)
        assertEquals(DriverApprovalStatus.APPROVED, domain.approvalStatus)
        assertTrue(domain.isOnline)
        assertEquals("https://example.com/avatar.jpg", domain.avatarUrl)
    }

    @Test
    fun `mapToDomain with null id uses fallbackId and default isOnline false`() {
        val dto = DriverAccountProfileResponse(
            id = null,
            fullName = null,
            phoneNumber = null,
            vehiclePlate = null,
            status = "PENDING_APPROVAL",
            isOnline = null,
            avatarUrl = null
        )

        val domain = DriverMapper.mapToDomain(dto, fallbackId = 100L)

        assertEquals(100L, domain.driverId)
        assertNull(domain.fullName)
        assertEquals(DriverApprovalStatus.PENDING_APPROVAL, domain.approvalStatus)
        assertFalse(domain.isOnline)
    }

    @Test
    fun `parseApprovalStatus handles all status strings`() {
        assertEquals(DriverApprovalStatus.APPROVED, DriverMapper.parseApprovalStatus("APPROVED"))
        assertEquals(DriverApprovalStatus.APPROVED, DriverMapper.parseApprovalStatus("active"))
        assertEquals(DriverApprovalStatus.PENDING_APPROVAL, DriverMapper.parseApprovalStatus("PENDING"))
        assertEquals(DriverApprovalStatus.PENDING_APPROVAL, DriverMapper.parseApprovalStatus("PENDING_APPROVAL"))
        assertEquals(DriverApprovalStatus.INACTIVE, DriverMapper.parseApprovalStatus("INACTIVE"))
        assertEquals(DriverApprovalStatus.REJECTED, DriverMapper.parseApprovalStatus("REJECTED"))
        assertEquals(DriverApprovalStatus.PENDING_APPROVAL, DriverMapper.parseApprovalStatus(null))
        assertEquals(DriverApprovalStatus.PENDING_APPROVAL, DriverMapper.parseApprovalStatus(""))
    }
}
