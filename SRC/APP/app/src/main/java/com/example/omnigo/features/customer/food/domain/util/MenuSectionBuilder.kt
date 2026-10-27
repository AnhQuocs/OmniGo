package com.example.omnigo.features.customer.food.domain.util

import com.example.omnigo.features.customer.food.domain.model.MenuItem

/** Một nhóm món theo danh mục. [category] rỗng nghĩa là món chưa phân loại. */
data class MenuSection(
    val category: String,
    val items: List<MenuItem>
) {
    val isUncategorized: Boolean get() = category.isBlank()
}

object MenuSectionBuilder {

    /**
     * Nhóm món theo category (giữ thứ tự xuất hiện), món chưa phân loại xếp cuối cùng
     * thay vì bị loại bỏ. Trong mỗi nhóm, món còn hàng đứng trước món hết hàng.
     * [query] lọc theo tên/mô tả/danh mục, không phân biệt dấu và hoa thường.
     */
    fun build(items: List<MenuItem>, query: String = ""): List<MenuSection> {
        val needle = query.normalizeForSearch()
        val filtered = if (needle.isEmpty()) {
            items
        } else {
            items.filter { item ->
                item.name.normalizeForSearch().contains(needle) ||
                    item.description.normalizeForSearch().contains(needle) ||
                    item.category.normalizeForSearch().contains(needle)
            }
        }

        val grouped = LinkedHashMap<String, MutableList<MenuItem>>()
        filtered.forEach { item ->
            grouped.getOrPut(item.category.trim()) { mutableListOf() }.add(item)
        }

        val sections = grouped.map { (category, list) ->
            MenuSection(
                category = category,
                items = list.sortedByDescending { it.isAvailable }
            )
        }
        return sections.filterNot { it.isUncategorized } + sections.filter { it.isUncategorized }
    }
}
