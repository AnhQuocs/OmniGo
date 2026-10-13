package com.example.omnigo.features.driver.data.mapper

import com.example.omnigo.features.driver.data.remote.dto.DriverAccountProfileResponse
import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus

object DriverMapper {

    fun mapToDomain(response: DriverAccountProfileResponse, fallbackId: Long = 0L): DriverAccountProfile {
        val status = parseApprovalStatus(response.status)
        return DriverAccountProfile(
            driverId = response.id ?: fallbackId,
            fullName = response.fullName,
            phoneNumber = response.phoneNumber,
            vehiclePlate = response.vehiclePlate,
            approvalStatus = status,
            isOnline = response.isOnline ?: false,
            avatarUrl = response.avatarUrl
        )
    }

    fun parseApprovalStatus(statusStr: String?): DriverApprovalStatus {
        return when (statusStr?.trim()?.uppercase()) {
            "APPROVED", "ACTIVE" -> DriverApprovalStatus.APPROVED
            "INACTIVE" -> DriverApprovalStatus.INACTIVE
            "REJECTED" -> DriverApprovalStatus.REJECTED
            "PENDING", "PENDING_APPROVAL" -> DriverApprovalStatus.PENDING_APPROVAL
            else -> if (statusStr.isNullOrBlank()) DriverApprovalStatus.PENDING_APPROVAL else DriverApprovalStatus.APPROVED
        }
    }
}
