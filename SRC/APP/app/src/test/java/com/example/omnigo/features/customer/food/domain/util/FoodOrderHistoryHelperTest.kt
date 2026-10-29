package com.example.omnigo.features.customer.food.domain.util

import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId

class FoodOrderHistoryHelperTest {

    @Test
    fun `resolveStatusGroup maps delivered, cancelled, and ongoing statuses accurately`() {
        assertEquals(OrderHistoryStatusGroup.DELIVERED, FoodOrderHistoryHelper.resolveStatusGroup("COMPLETED"))
        assertEquals(OrderHistoryStatusGroup.DELIVERED, FoodOrderHistoryHelper.resolveStatusGroup("delivered"))
        assertEquals(OrderHistoryStatusGroup.CANCELLED, FoodOrderHistoryHelper.resolveStatusGroup("CANCELLED"))
        assertEquals(OrderHistoryStatusGroup.CANCELLED, FoodOrderHistoryHelper.resolveStatusGroup("REJECTED"))
        assertEquals(OrderHistoryStatusGroup.ONGOING, FoodOrderHistoryHelper.resolveStatusGroup("DELIVERING"))
        assertEquals(OrderHistoryStatusGroup.ONGOING, FoodOrderHistoryHelper.resolveStatusGroup("PREPARING"))
        assertEquals(OrderHistoryStatusGroup.ONGOING, FoodOrderHistoryHelper.resolveStatusGroup("PENDING"))
    }

    @Test
    fun `resolvePaymentStatusType returns REFUNDED for cancelled paid or wallet order`() {
        val cancelledWalletOrder = sampleOrder(
            status = "CANCELLED",
            paymentMethod = PaymentMethod.WALLET,
            isPaid = true
        )
        val cancelledCardOrder = sampleOrder(
            status = "REJECTED",
            paymentMethod = PaymentMethod.VNPAY,
            isPaid = false
        )
        assertEquals(
            OrderPaymentStatusType.REFUNDED,
            FoodOrderHistoryHelper.resolvePaymentStatusType(cancelledWalletOrder)
        )
        assertEquals(
            OrderPaymentStatusType.REFUNDED,
            FoodOrderHistoryHelper.resolvePaymentStatusType(cancelledCardOrder)
        )
    }

    @Test
    fun `resolvePaymentStatusType returns PAID for online or paid order and CASH for cash order`() {
        val cashDelivered = sampleOrder(
            status = "COMPLETED",
            paymentMethod = PaymentMethod.CASH,
            isPaid = false
        )
        val walletOngoing = sampleOrder(
            status = "DELIVERING",
            paymentMethod = PaymentMethod.WALLET,
            isPaid = true
        )
        assertEquals(
            OrderPaymentStatusType.CASH,
            FoodOrderHistoryHelper.resolvePaymentStatusType(cashDelivered)
        )
        assertEquals(
            OrderPaymentStatusType.PAID,
            FoodOrderHistoryHelper.resolvePaymentStatusType(walletOngoing)
        )
    }

    @Test
    fun `resolveThumbnails calculates remaining quantity from total item quantities (Method 1)`() {
        // 2 distinct items with total quantity = 4 -> 2 thumbnails + "+2" remaining
        val fourItems = listOf(
            sampleItem(id = 1L, name = "Phở Bò", quantity = 2),
            sampleItem(id = 2L, name = "Quẩy", quantity = 2)
        )
        val summary4 = FoodOrderHistoryHelper.resolveThumbnails(fourItems)
        assertEquals(2, summary4.visibleItems.size)
        assertEquals(4, summary4.totalQuantity)
        assertEquals(2, summary4.remainingQuantity)

        // 3 distinct items with quantity = 1 each -> 2 thumbnails + "+1" remaining
        val threeItems = listOf(
            sampleItem(id = 1L, name = "Bún Bò", quantity = 1),
            sampleItem(id = 2L, name = "Rau sống", quantity = 1),
            sampleItem(id = 3L, name = "Trà đá", quantity = 1)
        )
        val summary3 = FoodOrderHistoryHelper.resolveThumbnails(threeItems)
        assertEquals(2, summary3.visibleItems.size)
        assertEquals(3, summary3.totalQuantity)
        assertEquals(1, summary3.remainingQuantity)

        // 2 distinct items with quantity = 1 each -> 2 thumbnails + 0 remaining
        val twoItems = listOf(
            sampleItem(id = 1L, name = "Trà sữa", quantity = 1),
            sampleItem(id = 2L, name = "Matcha", quantity = 1)
        )
        val summary2 = FoodOrderHistoryHelper.resolveThumbnails(twoItems)
        assertEquals(2, summary2.visibleItems.size)
        assertEquals(2, summary2.totalQuantity)
        assertEquals(0, summary2.remainingQuantity)
    }

    @Test
    fun `formatTimeAndDate formats ISO instant with time first then date`() {
        val formatted = FoodOrderHistoryHelper.formatTimeAndDate(
            rawDate = "2026-10-20T05:28:00Z",
            zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        )
        assertEquals("12:28 • 20/10/2026", formatted)
    }

    @Test
    fun `estimateOngoingEtaRange returns null for delivered or cancelled orders and range for ongoing`() {
        val delivered = sampleOrder(status = "COMPLETED", estimatedDeliveryMinutes = 15)
        val cancelled = sampleOrder(status = "CANCELLED", estimatedDeliveryMinutes = 15)
        val ongoingExplicit = sampleOrder(status = "DELIVERING", estimatedDeliveryMinutes = 15)

        assertNull(FoodOrderHistoryHelper.estimateOngoingEtaRange(delivered))
        assertNull(FoodOrderHistoryHelper.estimateOngoingEtaRange(cancelled))
        assertEquals(10 to 15, FoodOrderHistoryHelper.estimateOngoingEtaRange(ongoingExplicit))
    }

    @Test
    fun `enrichOrder fills missing restaurant image, address, and item images from catalog`() {
        val rawOrder = sampleOrder(
            status = "COMPLETED",
            restaurantImageUrl = null,
            restaurantAddress = null,
            items = listOf(
                sampleItem(id = 1L, menuItemId = 101L, name = "Phở Bò Tái Lăn", imageUrl = null),
                sampleItem(id = 2L, menuItemId = 102L, name = "Quẩy Giòn", imageUrl = null)
            )
        )
        val restaurant = Restaurant(
            id = 1L,
            name = "Phở Thìn Lò Đúc",
            phone = "0901234567",
            address = "13 Lò Đúc, Hai Bà Trưng, Hà Nội",
            latitude = 21.015,
            longitude = 105.855,
            imageUrl = "https://example.com/pho_thin.jpg",
            status = "ACTIVE",
            openTime = "06:00",
            closeTime = "22:00",
            rating = 4.8,
            reviewCount = 95,
            isOpen = true,
            menuItems = listOf(
                MenuItem(
                    id = 101L,
                    restaurantId = 1L,
                    name = "Phở Bò Tái Lăn",
                    description = "",
                    price = 65000.0,
                    imageUrl = "https://example.com/item_pho.jpg",
                    category = "Phở",
                    isAvailable = true
                ),
                MenuItem(
                    id = 102L,
                    restaurantId = 1L,
                    name = "Quẩy Giòn",
                    description = "",
                    price = 15000.0,
                    imageUrl = "https://example.com/item_quay.jpg",
                    category = "Món thêm",
                    isAvailable = true
                )
            )
        )

        val enriched = FoodOrderHistoryHelper.enrichOrder(rawOrder, restaurant)
        assertEquals("https://example.com/pho_thin.jpg", enriched.restaurantImageUrl)
        assertEquals("13 Lò Đúc, Hai Bà Trưng, Hà Nội", enriched.restaurantAddress)
        assertEquals("https://example.com/item_pho.jpg", enriched.items[0].imageUrl)
        assertEquals("https://example.com/item_quay.jpg", enriched.items[1].imageUrl)
    }

    private fun sampleItem(
        id: Long = 1L,
        menuItemId: Long = 10L,
        name: String = "Món ăn",
        quantity: Int = 1,
        imageUrl: String? = null
    ) = FoodOrderItem(
        id = id,
        menuItemId = menuItemId,
        itemName = name,
        itemPrice = 30000.0,
        quantity = quantity,
        subtotal = 30000.0 * quantity,
        note = "",
        imageUrl = imageUrl
    )

    private fun sampleOrder(
        status: String = "PENDING",
        paymentMethod: PaymentMethod? = PaymentMethod.CASH,
        isPaid: Boolean? = false,
        estimatedDeliveryMinutes: Int? = null,
        restaurantImageUrl: String? = null,
        restaurantAddress: String? = "Lò Đúc",
        items: List<FoodOrderItem> = emptyList()
    ) = FoodOrder(
        id = 1L,
        customerId = 10L,
        driverId = null,
        restaurantId = 1L,
        restaurantName = "Phở Thìn Lò Đúc",
        restaurantAddress = restaurantAddress,
        restaurantImageUrl = restaurantImageUrl,
        status = status,
        paymentMethod = paymentMethod,
        isPaid = isPaid,
        itemsPrice = 80000.0,
        deliveryFee = 15000.0,
        totalPrice = 95000.0,
        dropOffAddress = "123 Cầu Giấy",
        dropOffLatitude = 21.03,
        dropOffLongitude = 105.80,
        note = "",
        items = items,
        estimatedDeliveryMinutes = estimatedDeliveryMinutes,
        createdAt = "2026-10-20T05:28:00Z"
    )
}

