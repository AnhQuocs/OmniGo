package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import javax.inject.Inject

class UpdateDriverVehicleUseCase @Inject constructor(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(
        driverId: Long? = null,
        vehicleType: String,
        licensePlate: String,
        vehicleBrand: String
    ): Result<DriverAccountProfile> {
        if (licensePlate.isBlank()) {
            return Result.failure(IllegalArgumentException("Biển số xe không được để trống"))
        }
        return driverRepository.updateDriverVehicle(
            driverId = driverId,
            vehicleType = vehicleType,
            licensePlate = licensePlate,
            vehicleBrand = vehicleBrand
        )
    }
}
