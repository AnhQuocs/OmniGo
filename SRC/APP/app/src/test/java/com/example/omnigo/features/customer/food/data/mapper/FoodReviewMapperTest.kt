package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderReviewResponse
import com.example.omnigo.features.customer.food.domain.model.CreateFoodReviewCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodReviewMapperTest {

    @Test
    fun `toDomain maps full response DTO correctly`() {
        val dto = FoodOrderReviewResponse(
            id = 101L,
            orderId = 555L,
            restaurantRating = 5,
            restaurantComment = "Món ăn rất ngon, nóng hổi",
            driverRating = 5,
            driverTags = listOf("Giao hàng nhanh", "Thân thiện"),
            reviewPhotos = listOf("https://cdn.cloudinary.com/img1.jpg"),
            merchantReply = "Cảm ơn quý khách đã ủng hộ quán!",
            merchantReplyAt = "2026-10-23T10:00:00Z",
            createdAt = "2026-10-23T09:00:00Z",
            readOnly = true
        )

        val domain = dto.toDomain()

        assertEquals(101L, domain.id)
        assertEquals(555L, domain.orderId)
        assertEquals(5, domain.restaurantRating)
        assertEquals("Món ăn rất ngon, nóng hổi", domain.restaurantComment)
        assertEquals(5, domain.driverRating)
        assertEquals(listOf("Giao hàng nhanh", "Thân thiện"), domain.driverTags)
        assertEquals(listOf("https://cdn.cloudinary.com/img1.jpg"), domain.reviewPhotos)
        assertEquals("Cảm ơn quý khách đã ủng hộ quán!", domain.merchantReply)
        assertEquals("2026-10-23T10:00:00Z", domain.merchantReplyAt)
        assertEquals("2026-10-23T09:00:00Z", domain.createdAt)
        assertTrue(domain.isReadOnly)
    }

    @Test
    fun `toDomain handles null fields with safe defaults`() {
        val dto = FoodOrderReviewResponse(
            id = null,
            orderId = null,
            restaurantRating = null,
            restaurantComment = null,
            driverRating = null,
            driverTags = null,
            reviewPhotos = null,
            merchantReply = null,
            merchantReplyAt = null,
            createdAt = null,
            readOnly = null
        )

        val domain = dto.toDomain()

        assertEquals(0L, domain.id)
        assertEquals(0L, domain.orderId)
        assertEquals(5, domain.restaurantRating)
        assertNull(domain.restaurantComment)
        assertEquals(5, domain.driverRating)
        assertTrue(domain.driverTags.isEmpty())
        assertTrue(domain.reviewPhotos.isEmpty())
        assertNull(domain.merchantReply)
        assertNull(domain.createdAt)
        assertFalse(domain.isReadOnly)
    }

    @Test
    fun `toRequest maps command to DTO request correctly`() {
        val command = CreateFoodReviewCommand(
            orderId = 123L,
            restaurantRating = 4,
            restaurantComment = "Giao hàng tốt",
            driverRating = 5,
            driverTags = listOf("Đúng giờ"),
            reviewPhotos = listOf("https://cdn.cloudinary.com/photo.jpg")
        )

        val request = command.toRequest()

        assertEquals(123L, request.orderId)
        assertEquals(4, request.restaurantRating)
        assertEquals("Giao hàng tốt", request.restaurantComment)
        assertEquals(5, request.driverRating)
        assertEquals(listOf("Đúng giờ"), request.driverTags)
        assertEquals(listOf("https://cdn.cloudinary.com/photo.jpg"), request.reviewPhotos)
    }

    @Test
    fun `toRequest strips blank comment and empty collections`() {
        val command = CreateFoodReviewCommand(
            orderId = 123L,
            restaurantRating = 5,
            restaurantComment = "   ",
            driverRating = 5,
            driverTags = emptyList(),
            reviewPhotos = emptyList()
        )

        val request = command.toRequest()

        assertNull(request.restaurantComment)
        assertNull(request.driverTags)
        assertNull(request.reviewPhotos)
    }
}
