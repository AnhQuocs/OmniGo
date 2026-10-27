package com.example.omnigo.features.customer.food.domain.util

import com.example.omnigo.features.customer.food.domain.model.Restaurant

enum class RestaurantSort { RECOMMENDED, TOP_RATED, NEAREST }

object RestaurantListing {

    private const val MAX_CATEGORIES = 10

    /**
     * Danh mục lấy từ menu thực của các nhà hàng, ưu tiên danh mục có nhiều nhà hàng nhất.
     * Trả về rỗng nếu API không kèm menuItems.
     */
    fun deriveCategories(restaurants: List<Restaurant>): List<String> {
        val counts = LinkedHashMap<String, Pair<String, Int>>()
        restaurants.forEach { restaurant ->
            restaurant.menuItems
                .map { it.category.trim() }
                .filter { it.isNotEmpty() }
                .distinctBy { it.normalizeForSearch() }
                .forEach { category ->
                    val key = category.normalizeForSearch()
                    val current = counts[key]
                    counts[key] = (current?.first ?: category) to ((current?.second ?: 0) + 1)
                }
        }
        return counts.values
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
            .map { it.first }
            .take(MAX_CATEGORIES)
    }

    fun filterByCategory(restaurants: List<Restaurant>, category: String?): List<Restaurant> {
        if (category.isNullOrBlank()) return restaurants
        val key = category.normalizeForSearch()
        return restaurants.filter { restaurant ->
            restaurant.menuItems.any { it.category.normalizeForSearch() == key }
        }
    }

    /** Nhà hàng nhận đơn luôn đứng trước nhà hàng đóng/khóa; sắp xếp ổn định trong từng nhóm. */
    fun sort(
        restaurants: List<Restaurant>,
        sort: RestaurantSort,
        distancesKm: Map<Long, Double>
    ): List<Restaurant> {
        val orderable = compareByDescending<Restaurant> { it.canOrder }
        return when (sort) {
            RestaurantSort.RECOMMENDED -> restaurants.sortedWith(orderable)
            RestaurantSort.TOP_RATED -> restaurants.sortedWith(
                orderable.thenByDescending { it.rating }.thenByDescending { it.reviewCount }
            )
            RestaurantSort.NEAREST -> restaurants.sortedWith(
                orderable.thenBy { distancesKm[it.id] ?: Double.MAX_VALUE }
            )
        }
    }

    /** Nổi bật: chỉ các nhà hàng đang nhận đơn và thực sự có đánh giá. */
    fun featured(restaurants: List<Restaurant>, limit: Int = 6): List<Restaurant> =
        restaurants
            .filter { it.canOrder && it.hasReviews }
            .sortedWith(compareByDescending<Restaurant> { it.rating }.thenByDescending { it.reviewCount })
            .take(limit)
}
