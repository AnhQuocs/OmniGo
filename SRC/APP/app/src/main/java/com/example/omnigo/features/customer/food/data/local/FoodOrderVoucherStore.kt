package com.example.omnigo.features.customer.food.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backend food orders do not persist voucher info, so the applied voucher is stored locally per order id
 * to render the post-discount price on order detail/history.
 */
@Singleton
class FoodOrderVoucherStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(orderId: Long, code: String, discountAmount: Double) {
        if (discountAmount <= 0.0) return
        prefs.edit()
            .putString(codeKey(orderId), code)
            .putLong(discountKey(orderId), discountAmount.toLong())
            .apply()
    }

    fun get(orderId: Long): Pair<String, Double>? {
        val discount = prefs.getLong(discountKey(orderId), 0L)
        if (discount <= 0L) return null
        return (prefs.getString(codeKey(orderId), "") ?: "") to discount.toDouble()
    }

    private fun codeKey(orderId: Long) = "code_$orderId"
    private fun discountKey(orderId: Long) = "discount_$orderId"

    private companion object {
        const val PREFS_NAME = "food_order_vouchers"
    }
}
