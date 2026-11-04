package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import javax.inject.Inject

class ResubmitDriverProfileUseCase @Inject constructor(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(
        driverId: Long? = null,
        fullName: String,
        email: String? = null,
        licenseNumber: String,
        vehicleType: String,
        licensePlate: String,
        vehicleBrand: String,
        idCardFrontUrl: String? = null,
        idCardBackUrl: String? = null,
        licenseImageUrl: String? = null,
        vehicleRegImageUrl: String? = null
    ): Result<DriverAccountProfile> {
        return driverRepository.resubmitDriverProfile(
            driverId = driverId,
            fullName = fullName,
            email = email,
            licenseNumber = licenseNumber,
            vehicleType = vehicleType,
            licensePlate = licensePlate,
            vehicleBrand = vehicleBrand,
            idCardFrontUrl = idCardFrontUrl,
            idCardBackUrl = idCardBackUrl,
            licenseImageUrl = licenseImageUrl,
            vehicleRegImageUrl = vehicleRegImageUrl
        )
    }
}
