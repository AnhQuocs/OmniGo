package com.example.omnigo.features.driver.domain.model

data class DriverAccountProfile(
    val driverId: Long,
    val fullName: String?,
    val phoneNumber: String?,
    val vehiclePlate: String?,
    val approvalStatus: DriverApprovalStatus,
    val isOnline: Boolean = false,
    val avatarUrl: String? = null,
    val vehicleType: String? = null,
    val vehicleBrand: String? = null,
    val licenseNumber: String? = null,
    val idCardFrontUrl: String? = null,
    val idCardBackUrl: String? = null,
    val licenseImageUrl: String? = null,
    val vehicleRegImageUrl: String? = null
)
