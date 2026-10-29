package com.example.omnigo.features.customer.food.domain.util

import com.example.omnigo.features.customer.food.domain.model.MenuItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RestaurantPriceRangeHelperTest {

    private fun createItem(
        id: Long,
        price: Double,
        isAvailable: Boolean = true
    ) = MenuItem(
        id = id,
        restaurantId = 1L,
        name = "Item $id",
        description = "",
        price = price,
        imageUrl = "",
        category = "Cơm",
        isAvailable = isAvailable
    )

    @Test
    fun `calculate returns null when items list is empty`() {
        val result = RestaurantPriceRangeHelper.calculate(emptyList())
        assertNull(result)
    }

    @Test
    fun `calculate returns null when all items have invalid or zero price`() {
        val items = listOf(
            createItem(1, 0.0),
            createItem(2, -1000.0)
        )
        val result = RestaurantPriceRangeHelper.calculate(items)
        assertNull(result)
    }

    @Test
    fun `calculate identifies single price when all items have identical price`() {
        val items = listOf(
            createItem(1, 35000.0),
            createItem(2, 35000.0)
        )
        val result = RestaurantPriceRangeHelper.calculate(items)
        assertNotNull(result)
        assertEquals(35000.0, result!!.minPrice, 0.001)
        assertEquals(35000.0, result.maxPrice, 0.001)
        assertTrue(result.isSinglePrice)
    }

    @Test
    fun `calculate identifies min and max price across diverse items`() {
        val items = listOf(
            createItem(1, 20000.0),
            createItem(2, 85000.0),
            createItem(3, 45000.0)
        )
        val result = RestaurantPriceRangeHelper.calculate(items)
        assertNotNull(result)
        assertEquals(20000.0, result!!.minPrice, 0.001)
        assertEquals(85000.0, result.maxPrice, 0.001)
        assertFalse(result.isSinglePrice)
    }

    @Test
    fun `calculate prioritizes available items over sold out items`() {
        val items = listOf(
            createItem(1, 10000.0, isAvailable = false),
            createItem(2, 30000.0, isAvailable = true),
            createItem(3, 70000.0, isAvailable = true),
            createItem(4, 95000.0, isAvailable = false)
        )
        val result = RestaurantPriceRangeHelper.calculate(items)
        assertNotNull(result)
        assertEquals(30000.0, result!!.minPrice, 0.001)
        assertEquals(70000.0, result.maxPrice, 0.001)
    }

    @Test
    fun `calculate falls back to all items when all items are sold out`() {
        val items = listOf(
            createItem(1, 25000.0, isAvailable = false),
            createItem(2, 60000.0, isAvailable = false)
        )
        val result = RestaurantPriceRangeHelper.calculate(items)
        assertNotNull(result)
        assertEquals(25000.0, result!!.minPrice, 0.001)
        assertEquals(60000.0, result.maxPrice, 0.001)
    }

    @Test
    fun `formatPriceRange returns default fallback when items list is empty`() {
        val formatted = RestaurantPriceRangeHelper.formatPriceRange(emptyList())
        assertEquals(RestaurantPriceRangeHelper.DEFAULT_PRICE_RANGE_FALLBACK, formatted)
    }

    @Test
    fun `formatPriceRange returns single price string when min equals max`() {
        val items = listOf(createItem(1, 35000.0))
        val formatted = RestaurantPriceRangeHelper.formatPriceRange(items)
        assertTrue(formatted.contains("35.000") || formatted.contains("35,000"))
        assertTrue(formatted.endsWith("đ"))
        assertFalse(formatted.contains(" - "))
    }

    @Test
    fun `formatPriceRange returns range string when min is less than max`() {
        val items = listOf(
            createItem(1, 15000.0),
            createItem(2, 80000.0)
        )
        val formatted = RestaurantPriceRangeHelper.formatPriceRange(items)
        assertTrue(formatted.contains(" - "))
        assertTrue(formatted.endsWith("đ"))
    }
}
