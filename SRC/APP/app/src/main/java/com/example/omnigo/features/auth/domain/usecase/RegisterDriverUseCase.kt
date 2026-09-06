package com.example.omnigo.features.auth.domain.usecase

import com.example.omnigo.features.auth.domain.error.RegisterError
import com.example.omnigo.features.auth.domain.model.RegisterDriver
import com.example.omnigo.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterDriverUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        driver: RegisterDriver
    ): RegisterResult {

        validate(driver)?.let {
            return RegisterResult.Error(it)
        }

        return authRepository.registerDriver(driver)
    }

    private fun validate(driver: RegisterDriver): RegisterError? {
        val user = driver.user

        if (user.fullName.isBlank()) {
            return RegisterError.EMPTY_FULL_NAME
        }

        if (user.email.isBlank()) {
            return RegisterError.EMPTY_EMAIL
        }

        if (!user.email.matches(EMAIL_REGEX)) {
            return RegisterError.INVALID_EMAIL
        }

        if (user.phoneNumber.isBlank()) {
            return RegisterError.EMPTY_PHONE
        }

        if (!com.example.omnigo.utils.PhoneUtils.isValidVietnamesePhoneNumber(user.phoneNumber)) {
            return RegisterError.INVALID_PHONE
        }

        if (user.password.isBlank()) {
            return RegisterError.EMPTY_PASSWORD
        }

        if (user.password.length < 6) {
            return RegisterError.PASSWORD_TOO_SHORT
        }

        if (driver.licensePlate.isBlank()) {
            return RegisterError.EMPTY_LICENSE_PLATE
        }

        if (driver.vehicleModel.isBlank()) {
            return RegisterError.EMPTY_VEHICLE_MODEL
        }

        return null
    }

    companion object {
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}