package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import javax.inject.Inject

class GetDriverProfileUseCase @Inject constructor(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(driverId: Long?): Result<DriverAccountProfile> {
        return driverRepository.getDriverProfile(driverId)
    }
}
