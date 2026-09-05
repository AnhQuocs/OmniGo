package com.example.omnigo.features.auth.data.mapper

import com.example.omnigo.features.auth.data.remote.dto.request.CustomerRegisterRequest
import com.example.omnigo.features.auth.data.remote.dto.request.DriverRegisterRequest
import com.example.omnigo.features.auth.data.remote.dto.response.UserResponse
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.model.DriverInfo
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.model.RegisterUser
import com.example.omnigo.features.auth.domain.model.UserRole
import com.example.omnigo.features.auth.domain.model.UserStatus
import com.example.omnigo.features.auth.domain.model.VehicleType
import java.time.LocalDateTime

fun UserResponse.toDomain(): AuthUser {
    val userRole = UserRole.entries.find { it.name.equals(role.trim(), ignoreCase = true) } ?: UserRole.CUSTOMER

    val userStatus = status?.trim()?.let { s ->
        UserStatus.entries.find { it.name.equals(s, ignoreCase = true) }
    }

    val parsedDriverInfo = if (userRole == UserRole.DRIVER) {
        val parsedVehicleType = vehicleType?.trim()?.let { v ->
            VehicleType.entries.find { it.name.equals(v, ignoreCase = true) }
        } ?: VehicleType.BIKE

        DriverInfo(
            vehicleType = parsedVehicleType,
            licensePlate = licensePlate.orEmpty(),
            vehicleModel = vehicleModel.orEmpty()
        )
    } else {
        null
    }

    return AuthUser(
        id = id,
        phoneNumber = phoneNumber,
        email = email,
        fullName = fullName,
        role = userRole,
        status = userStatus,
        driverInfo = parsedDriverInfo,
        createdAt = parseCreatedAtSafely(createdAt)
    )
}

private fun parseCreatedAtSafely(dateString: String?): LocalDateTime {
    if (dateString.isNullOrBlank()) return LocalDateTime.now()
    return runCatching {
        LocalDateTime.parse(dateString)
    }.recoverCatching {
        val instant = java.time.Instant.parse(dateString)
        LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
    }.recoverCatching {
        val zdt = java.time.ZonedDateTime.parse(dateString)
        zdt.toLocalDateTime()
    }.getOrDefault(LocalDateTime.now())
}

fun RegisterUser.toCustomerRequest(firebaseToken: String? = null) = CustomerRegisterRequest(
    email = email,
    password = password,
    fullName = fullName,
    phoneNumber = phoneNumber,
    firebaseToken = firebaseToken
)

fun RegisterDriver.toDriverRequest(firebaseToken: String? = null) = DriverRegisterRequest(
    email = user.email,
    password = user.password,
    fullName = user.fullName,
    phoneNumber = user.phoneNumber,
    firebaseToken = firebaseToken,
    vehicleType = vehicleType.name,
    licensePlate = licensePlate,
    vehicleModel = vehicleModel
)