package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.DriverLocationEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderAssignmentEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderStatusEventDto
import com.example.omnigo.features.customer.food.domain.model.DriverLocation
import com.example.omnigo.features.customer.food.domain.model.FoodOrderAssignmentEvent
import com.example.omnigo.features.customer.food.domain.model.FoodOrderStatusEvent

fun FoodOrderStatusEventDto.toDomain(): FoodOrderStatusEvent? {
    val validOrderId = orderId?.takeIf { it > 0 } ?: return null
    return FoodOrderStatusEvent(
        orderId = validOrderId,
        status = status,
        timestamp = timestamp
    )
}

fun FoodOrderAssignmentEventDto.toDomain(): FoodOrderAssignmentEvent? {
    val validOrderId = orderId?.takeIf { it > 0 } ?: return null
    val validDriverId = driverId?.takeIf { it > 0 } ?: return null
    return FoodOrderAssignmentEvent(
        orderId = validOrderId,
        driverId = validDriverId,
        driverName = driverName,
        driverPhone = driverPhone,
        vehiclePlate = vehiclePlate
    )
}

fun DriverLocationEventDto.toDomain(): DriverLocation? {
    val validDriverId = driverId?.takeIf { it > 0 } ?: return null
    val validLatitude = latitude?.takeIf { it in -90.0..90.0 } ?: return null
    val validLongitude = longitude?.takeIf { it in -180.0..180.0 } ?: return null
    return DriverLocation(
        driverId = validDriverId,
        latitude = validLatitude,
        longitude = validLongitude,
        bearing = bearing,
        speed = speed,
        timestamp = timestamp
    )
}
