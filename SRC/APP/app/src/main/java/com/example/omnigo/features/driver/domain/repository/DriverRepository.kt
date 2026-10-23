package com.example.omnigo.features.driver.domain.repository

import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverLocationUpdate
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder

interface DriverRepository {
    suspend fun updateDriverStatus(driverId: Long?, isOnline: Boolean): Result<Boolean>
    suspend fun getDriverProfile(driverId: Long?): Result<DriverAccountProfile>
    suspend fun sendDriverLocation(locationUpdate: DriverLocationUpdate): Result<Boolean>
    suspend fun acceptFoodOrder(orderId: Long): Result<Boolean>
    suspend fun rejectFoodOrder(orderId: Long): Result<Boolean>
    suspend fun getDriverFoodOrderDetail(orderId: Long): Result<DriverDeliveryOrder>
    suspend fun getDriverActiveFoodOrders(): Result<List<DriverDeliveryOrder>>
    suspend fun updateDriverOrderStatus(
        orderId: Long,
        status: String,
        reason: String? = null,
        reasonCode: String? = null
    ): Result<DriverDeliveryOrder>
}
