package com.example.omnigo.features.driver.domain.repository

import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverLocationUpdate

interface DriverRepository {
    suspend fun updateDriverStatus(driverId: Long?, isOnline: Boolean): Result<Boolean>
    suspend fun getDriverProfile(driverId: Long?): Result<DriverAccountProfile>
    suspend fun sendDriverLocation(locationUpdate: DriverLocationUpdate): Result<Boolean>
}
