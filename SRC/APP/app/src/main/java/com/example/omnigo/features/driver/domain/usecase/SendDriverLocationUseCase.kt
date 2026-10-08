package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.driver.domain.model.DriverLocationUpdate
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import javax.inject.Inject

class SendDriverLocationUseCase @Inject constructor(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(locationUpdate: DriverLocationUpdate): Result<Boolean> {
        return driverRepository.sendDriverLocation(locationUpdate)
    }
}
