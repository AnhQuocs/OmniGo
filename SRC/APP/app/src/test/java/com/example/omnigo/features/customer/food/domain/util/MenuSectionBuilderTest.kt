package com.example.omnigo.features.customer.food.domain.util

import com.example.omnigo.features.customer.food.domain.model.MenuItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuSectionBuilderTest {

    private fun item(
        id: Long,
        name: String,
        category: String,
        isAvailable: Boolean = true,
        desc: String = ""
    ) = MenuItem(
        id = id,
        restaurantId = 1L,
        name = name,
        description = desc,
        price = 30000.0,
        imageUrl = "",
        category = category,
        isAvailable = isAvailable
    )

    @Test
    fun `groups by category preserving order and puts uncategorized items last`() {
        val items = listOf(
            item(1, "Phở Tái", "Phở"),
            item(2, "Bánh Mì Trứng", ""),
            item(3, "Phở Gà", "Phở"),
            item(4, "Trà Chanh", "Đồ uống"),
            item(5, "Nước Lọc", "   ")
        )

        val sections = MenuSectionBuilder.build(items)

        assertEquals(3, sections.size)
        assertEquals("Phở", sections[0].category)
        assertEquals(listOf(1L, 3L), sections[0].items.map { it.id })

        assertEquals("Đồ uống", sections[1].category)
        assertEquals(listOf(4L), sections[1].items.map { it.id })

        assertTrue(sections[2].isUncategorized)
        assertEquals(listOf(2L, 5L), sections[2].items.map { it.id })
    }

    @Test
    fun `sorts available items before sold out within each section`() {
        val items = listOf(
            item(1, "Món 1", "Món Chính", isAvailable = false),
            item(2, "Món 2", "Món Chính", isAvailable = true),
            item(3, "Món 3", "Món Chính", isAvailable = false),
            item(4, "Món 4", "Món Chính", isAvailable = true)
        )

        val sections = MenuSectionBuilder.build(items)
        val sectionItems = sections.first().items

        assertTrue(sectionItems[0].isAvailable)
        assertTrue(sectionItems[1].isAvailable)
        assertFalse(sectionItems[2].isAvailable)
        assertFalse(sectionItems[3].isAvailable)
    }

    @Test
    fun `filters items by accent-insensitive search query across name, description, category`() {
        val items = listOf(
            item(1, "Phở Bò Tái Lăn", "Món Nước", desc = "Hành tây xào"),
            item(2, "Cơm Rang Dưa Bò", "Cơm", desc = "Dưa chua giòn"),
            item(3, "Trà Đào Cam Sả", "Đồ Uống", desc = "Thơm mát")
        )

        val result1 = MenuSectionBuilder.build(items, query = "pho")
        assertEquals(1, result1.size)
        assertEquals(1L, result1[0].items.first().id)

        // Match description "xao" with accent "xào"
        val result2 = MenuSectionBuilder.build(items, query = "xao")
        assertEquals(1L, result2[0].items.first().id)

        // Match category "mon nuoc"
        val result3 = MenuSectionBuilder.build(items, query = "mon nuoc")
        assertEquals(1L, result3[0].items.first().id)
    }
}
