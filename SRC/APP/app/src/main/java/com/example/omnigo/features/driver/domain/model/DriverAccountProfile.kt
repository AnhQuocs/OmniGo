package com.example.omnigo.features.driver.domain.model

data class DriverAccountProfile(
    val driverId: Long,
    val fullName: String?,
    val phoneNumber: String?,
    val vehiclePlate: String?,
    val approvalStatus: DriverApprovalStatus,
    val isOnline: Boolean = false,
    val avatarUrl: String? = null
)
