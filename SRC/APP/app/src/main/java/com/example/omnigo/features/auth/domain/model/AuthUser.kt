package com.example.omnigo.features.auth.domain.model

import java.time.LocalDateTime

enum class UserRole {
    CUSTOMER,
    DRIVER
}

enum class UserStatus {
    ACTIVE, INACTIVE, PENDING, REJECTED, APPROVED
}

enum class VehicleType {
    BIKE, CAR_4_SEAT, CAR_7_SEAT, EXPRESS
}

data class AuthUser(
    val id: Long,
    val phoneNumber: String,
    val email: String?,
    val fullName: String,
    val role: UserRole,
    val status: UserStatus?,
    val driverInfo: DriverInfo?,
    val createdAt: LocalDateTime
)

data class DriverInfo(
    val vehicleType: VehicleType,
    val licensePlate: String,
    val vehicleModel: String
)