package com.example.omnigo.features.customer.food.domain.util

import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import org.junit.Assert.assertEquals
import org.junit.Test

class RestaurantListingTest {

    private fun restaurant(
        id: Long,
        name: String,
        rating: Double = 4.5,
        reviewCount: Int = 10,
        isOpen: Boolean = true,
        isLocked: Boolean = false,
        categories: List<String> = emptyList()
    ): Restaurant {
        val menu = categories.mapIndexed { idx, cat ->
            MenuItem(
                id = idx.toLong() + 1,
                restaurantId = id,
                name = "Item $idx",
                description = "",
                price = 20000.0,
                imageUrl = "",
                category = cat,
                isAvailable = true
            )
        }
        return Restaurant(
            id = id,
            name = name,
            phone = "0123456789",
            address = "Address $id",
            latitude = 21.0,
            longitude = 105.0,
            imageUrl = "",
            status = if (isOpen) "OPEN" else "CLOSED",
            openTime = "07:00",
            closeTime = "22:00",
            rating = rating,
            reviewCount = reviewCount,
            isOpen = isOpen,
            menuItems = menu,
            isLocked = isLocked
        )
    }

    @Test
    fun `deriveCategories ranks categories by occurrence across restaurants`() {
        val r1 = restaurant(1, "R1", categories = listOf("Cơm", "Đồ uống"))
        val r2 = restaurant(2, "R2", categories = listOf("Phở", "Cơm"))
        val r3 = restaurant(3, "R3", categories = listOf("Bánh Mì", "Cơm", "Đồ uống"))

        val derived = RestaurantListing.deriveCategories(listOf(r1, r2, r3))

        assertEquals("Cơm", derived[0])
        assertEquals("Đồ uống", derived[1])
    }

    @Test
    fun `sort TOP_RATED prioritizes orderable then rating then review count`() {
        val rClosedHighRating = restaurant(1, "Closed High", rating = 5.0, reviewCount = 100, isOpen = false)
        val rOpenLowRating = restaurant(2, "Open Low", rating = 4.0, reviewCount = 5, isOpen = true)
        val rOpenHighRating = restaurant(3, "Open High", rating = 4.9, reviewCount = 50, isOpen = true)

        val sorted = RestaurantListing.sort(
            listOf(rClosedHighRating, rOpenLowRating, rOpenHighRating),
            RestaurantSort.TOP_RATED,
            distancesKm = emptyMap()
        )

        assertEquals(3L, sorted[0].id)
        assertEquals(2L, sorted[1].id)
        assertEquals(1L, sorted[2].id) // Closed placed last
    }

    @Test
    fun `sort NEAREST respects distance map`() {
        val rFar = restaurant(1, "Far", isOpen = true)
        val rNear = restaurant(2, "Near", isOpen = true)
        val distances = mapOf(1L to 5.5, 2L to 1.2)

        val sorted = RestaurantListing.sort(listOf(rFar, rNear), RestaurantSort.NEAREST, distances)
        assertEquals(2L, sorted[0].id)
        assertEquals(1L, sorted[1].id)
    }

    @Test
    fun `featured excludes restaurants without reviews or closed`() {
        val rValid = restaurant(1, "Valid", rating = 4.8, reviewCount = 20, isOpen = true)
        val rNoReviews = restaurant(2, "No reviews", rating = 0.0, reviewCount = 0, isOpen = true)
        val rClosed = restaurant(3, "Closed", rating = 5.0, reviewCount = 100, isOpen = false)

        val featured = RestaurantListing.featured(listOf(rValid, rNoReviews, rClosed))
        assertEquals(1, featured.size)
        assertEquals(1L, featured[0].id)
    }
}
