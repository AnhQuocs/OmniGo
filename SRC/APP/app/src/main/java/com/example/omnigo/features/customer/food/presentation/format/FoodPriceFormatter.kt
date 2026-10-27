package com.example.omnigo.features.customer.food.presentation.format

import java.text.NumberFormat
import java.util.Locale

/** Nguồn định dạng giá duy nhất cho toàn bộ luồng đặt món (list, chi tiết, giỏ hàng, đơn hàng). */
object FoodPriceFormatter {

    /**
     * Trả về số tiền đã nhóm hàng nghìn theo locale (không kèm ký hiệu tiền tệ),
     * hoặc null khi dữ liệu không hợp lệ (âm, NaN, vô cực).
     */
    fun formatAmount(amount: Double?, locale: Locale = Locale.getDefault()): String? {
        if (amount == null || amount.isNaN() || amount.isInfinite() || amount < 0.0) return null
        return NumberFormat.getIntegerInstance(locale).format(Math.round(amount))
    }
}
