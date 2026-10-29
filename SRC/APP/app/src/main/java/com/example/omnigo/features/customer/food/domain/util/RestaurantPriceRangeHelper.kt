package com.example.omnigo.features.customer.food.domain.util

import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.presentation.format.FoodPriceFormatter

/**
 * Đại diện cho khoảng giá (thấp nhất - cao nhất) của các món ăn tại nhà hàng.
 */
data class FoodPriceRange(
    val minPrice: Double,
    val maxPrice: Double
) {
    val isSinglePrice: Boolean get() = minPrice == maxPrice
}

object RestaurantPriceRangeHelper {

    const val DEFAULT_PRICE_RANGE_FALLBACK = "15.000đ - 60.000đ"

    /**
     * Trích xuất mức giá thấp nhất và cao nhất từ danh sách món ăn.
     * Bỏ qua các món có giá <= 0 hoặc không hợp lệ.
     * Ưu tiên các món đang còn hàng (isAvailable = true), nếu tất cả đã hết hàng thì xét chung.
     */
    fun calculate(items: List<MenuItem>): FoodPriceRange? {
        val validItems = items.filter { it.price > 0 && !it.price.isNaN() && !it.price.isInfinite() }
        if (validItems.isEmpty()) return null

        val targetItems = validItems.filter { it.isAvailable }.ifEmpty { validItems }
        val min = targetItems.minOfOrNull { it.price } ?: return null
        val max = targetItems.maxOfOrNull { it.price } ?: return null

        return FoodPriceRange(minPrice = min, maxPrice = max)
    }

    /**
     * Định dạng khoảng giá hiển thị cho người dùng (ví dụ: "15.000đ - 80.000đ" hoặc "35.000đ").
     * Trả về [defaultText] nếu menu rỗng hoặc chưa tải.
     */
    fun formatPriceRange(
        items: List<MenuItem>,
        defaultText: String = DEFAULT_PRICE_RANGE_FALLBACK
    ): String {
        val range = calculate(items) ?: return defaultText
        val minStr = FoodPriceFormatter.formatAmount(range.minPrice) ?: return defaultText
        if (range.isSinglePrice) {
            return "${minStr}đ"
        }
        val maxStr = FoodPriceFormatter.formatAmount(range.maxPrice) ?: return defaultText
        return "${minStr}đ - ${maxStr}đ"
    }
}
