package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.MenuItemResponse
import com.example.omnigo.features.customer.food.data.remote.dto.RestaurantResponse
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant

fun MenuItemResponse.toDomain(): MenuItem {
    return MenuItem(
        id = id,
        restaurantId = restaurantId,
        name = name,
        description = description.orEmpty(),
        price = price,
        imageUrl = imageUrl.orEmpty(),
        category = category.orEmpty(),
        isAvailable = isAvailable
    )
}

fun RestaurantResponse.toDomain(): Restaurant {
    return Restaurant(
        id = id,
        name = name,
        phone = phone.orEmpty(),
        address = address.orEmpty(),
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        imageUrl = imageUrl.orEmpty(),
        status = status.orEmpty(),
        openTime = openTime.orEmpty(),
        closeTime = closeTime.orEmpty(),
        rating = rating ?: 0.0,
        reviewCount = reviewCount ?: totalReviews ?: 0,
        isOpen = status?.uppercase() == "OPEN",
        menuItems = menuItems?.map { it.toDomain() }.orEmpty()
    )
}
