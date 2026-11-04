package com.example.omnigo.features.customer.home.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HomeFilterCoordinatorTest {

    private lateinit var coordinator: HomeFilterCoordinator

    private val sampleMenu = listOf(
        MenuItem(
            id = 1L,
            restaurantId = 1L,
            name = "Phở Bò",
            description = "Phở bò thơm ngon",
            price = 45000.0,
            imageUrl = "",
            category = "Món chính",
            isAvailable = true
        )
    )

    private val rClose = Restaurant(
        id = 1L,
        name = "Quán Gần 1km",
        phone = "0900000001",
        address = "123 Đường A",
        latitude = 21.0285, // ~1km from 21.02
        longitude = 105.8542,
        imageUrl = "",
        status = "APPROVED",
        openTime = "07:00",
        closeTime = "22:00",
        rating = 4.9,
        reviewCount = 120,
        isOpen = true,
        menuItems = sampleMenu
    )

    private val rFar = Restaurant(
        id = 2L,
        name = "Quán Xa 10km",
        phone = "0900000002",
        address = "456 Đường B",
        latitude = 21.1285, // ~12km
        longitude = 105.8542,
        imageUrl = "",
        status = "APPROVED",
        openTime = "08:00",
        closeTime = "21:00",
        rating = 4.2,
        reviewCount = 45,
        isOpen = false,
        menuItems = emptyList()
    )

    private val rTopRated = Restaurant(
        id = 3L,
        name = "Quán 5 Sao",
        phone = "0900000003",
        address = "789 Đường C",
        latitude = 21.0585,
        longitude = 105.8542,
        imageUrl = "",
        status = "APPROVED",
        openTime = "06:00",
        closeTime = "23:00",
        rating = 5.0,
        reviewCount = 300,
        isOpen = true,
        menuItems = sampleMenu
    )

    @Before
    fun setUp() {
        coordinator = HomeFilterCoordinator()
    }

    @Test
    fun `filterAndSort returns empty list when input is empty`() {
        val result = coordinator.filterAndSort(emptyList(), HomeFilter.ALL, 21.02, 105.85)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterAndSort returns unchanged list when filter is ALL`() {
        val input = listOf(rFar, rClose, rTopRated)
        val result = coordinator.filterAndSort(input, HomeFilter.ALL, 21.02, 105.85)
        assertEquals(3, result.size)
        assertEquals(input, result)
    }

    @Test
    fun `filterAndSort sorts by distance when filter is NEARBY and coordinates provided`() {
        val input = listOf(rFar, rClose)
        val result = coordinator.filterAndSort(input, HomeFilter.NEARBY, 21.02, 105.85)
        assertEquals(2, result.size)
        assertEquals(rClose.id, result[0].id)
        assertEquals(rFar.id, result[1].id)
    }

    @Test
    fun `filterAndSort keeps order when filter is NEARBY but user coordinates null`() {
        val input = listOf(rFar, rClose)
        val result = coordinator.filterAndSort(input, HomeFilter.NEARBY, null, null)
        assertEquals(input, result)
    }

    @Test
    fun `filterAndSort filters high rated restaurants when filter is TOP_RATED`() {
        val input = listOf(rFar, rClose, rTopRated)
        val result = coordinator.filterAndSort(input, HomeFilter.TOP_RATED, 21.02, 105.85)
        assertEquals(2, result.size)
        assertEquals(rTopRated.id, result[0].id) // 5.0
        assertEquals(rClose.id, result[1].id)    // 4.9
    }

    @Test
    fun `filterAndSort prioritizes open and menu rich places when filter is PROMOS`() {
        val input = listOf(rFar, rClose, rTopRated)
        val result = coordinator.filterAndSort(input, HomeFilter.PROMOS, 21.02, 105.85)
        assertEquals(3, result.size)
        // rFar is closed so it must be last
        assertEquals(rFar.id, result[2].id)
    }
}

