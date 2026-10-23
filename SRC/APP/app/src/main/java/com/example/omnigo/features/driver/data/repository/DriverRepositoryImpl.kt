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

    override suspend fun acceptFoodOrder(orderId: Long): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = driverApi.acceptFoodOrder(orderId)
                if (response.success) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to accept food order"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun rejectFoodOrder(orderId: Long): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = driverApi.rejectFoodOrder(orderId)
                if (response.success) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to reject food order"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getDriverFoodOrderDetail(orderId: Long): Result<com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder> {
        return withContext(Dispatchers.IO) {
            try {
                val response = driverApi.getFoodOrderDetail(orderId)
                val body = response.data
                if (response.success && body != null) {
                    val domainOrder = com.example.omnigo.features.driver.food.data.mapper.DriverDeliveryOrderMapper.mapToDomain(body)
                    if (domainOrder != null) {
                        Result.success(domainOrder)
                    } else {
                        Result.failure(Exception("Failed to parse delivery order data"))
                    }
                } else {
                    Result.failure(Exception(response.message ?: "Failed to get delivery order detail"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getDriverActiveFoodOrders(): Result<List<com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = driverApi.getDriverFoodOrders()
                val body = response.data
                if (response.success && body != null) {
                    val orders = body.mapNotNull {
                        com.example.omnigo.features.driver.food.data.mapper.DriverDeliveryOrderMapper.mapToDomain(it)
                    }.filter { it.status.isActive }
                    Result.success(orders)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to fetch driver food orders"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateDriverOrderStatus(
        orderId: Long,
        status: String,
        reason: String?,
        reasonCode: String?
    ): Result<com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder> {
        return withContext(Dispatchers.IO) {
            try {
                val request = com.example.omnigo.features.driver.food.data.remote.dto.UpdateDriverOrderStatusRequest(
                    status = status,
                    reason = reason,
                    reasonCode = reasonCode
                )
                val response = driverApi.updateDriverOrderStatus(orderId, request)
                val body = response.data
                if (response.success && body != null) {
                    val domainOrder = com.example.omnigo.features.driver.food.data.mapper.DriverDeliveryOrderMapper.mapToDomain(body)
                    if (domainOrder != null) {
                        Result.success(domainOrder)
                    } else {
                        Result.failure(Exception("Failed to parse updated delivery order data"))
                    }
                } else {
                    Result.failure(Exception(response.message ?: "Failed to update driver order status"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
