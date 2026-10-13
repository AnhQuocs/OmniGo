package com.example.omnigo.features.driver.data.repository

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.driver.data.mapper.DriverMapper
import com.example.omnigo.features.driver.data.remote.api.DriverApi
import com.example.omnigo.features.driver.data.remote.dto.DriverLocationRequest
import com.example.omnigo.features.driver.data.remote.dto.DriverStatusUpdateRequest
import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverLocationUpdate
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverRepositoryImpl @Inject constructor(
    private val driverApi: DriverApi,
    private val sessionManager: SessionManager
) : DriverRepository {

    override suspend fun updateDriverStatus(driverId: Long?, isOnline: Boolean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val statusStr = if (isOnline) "ONLINE" else "OFFLINE"
                val request = DriverStatusUpdateRequest(status = statusStr, isOnline = isOnline)

                val targetId = driverId ?: sessionManager.getUserId()
                val response = if (targetId != null && targetId > 0L) {
                    try {
                        driverApi.updateDriverStatus(targetId, request)
                    } catch (e: Exception) {
                        driverApi.updateMyDriverStatus(request)
                    }
                } else {
                    driverApi.updateMyDriverStatus(request)
                }

                if (response.success) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to update driver status"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getDriverProfile(driverId: Long?): Result<DriverAccountProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val targetId = driverId ?: sessionManager.getUserId() ?: 0L
                val response = if (targetId > 0L) {
                    try {
                        driverApi.getDriverProfileById(targetId)
                    } catch (e: Exception) {
                        try {
                            driverApi.getMyDriverProfile()
                        } catch (e2: Exception) {
                            driverApi.getDriverProfileInternal(targetId)
                        }
                    }
                } else {
                    driverApi.getMyDriverProfile()
                }

                val data = response.data
                if (data != null) {
                    val domainProfile = DriverMapper.mapToDomain(data, fallbackId = targetId)
                    Result.success(domainProfile)
                } else {
                    Result.failure(Exception(response.message ?: "Driver profile response empty"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun sendDriverLocation(locationUpdate: DriverLocationUpdate): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val request = DriverLocationRequest(
                    latitude = locationUpdate.latitude,
                    longitude = locationUpdate.longitude,
                    bearing = locationUpdate.bearing,
                    speed = locationUpdate.speed,
                    timestamp = locationUpdate.timestamp
                )

                val response = driverApi.sendDriverLocation(request)
                if (response.success) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to send driver location"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
