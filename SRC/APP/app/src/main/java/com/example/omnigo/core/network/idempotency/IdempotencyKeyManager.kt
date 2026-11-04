package com.example.omnigo.core.network.idempotency

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quản lý Idempotency-Key (UUID v4) cho các request POST quan trọng:
 * - POST /api/v1/bookings
 * - POST /api/v1/food-orders
 * - POST /api/v1/payments/payment/create
 *
 * Đảm bảo:
 * 1. Mỗi ý định (scope + fingerprint) dùng 1 key duy nhất (16-128 ký tự, UUID v4).
 * 2. Lưu bền vững vào SharedPreferences để khi mất mạng, timeout, hoặc app khởi động lại
 *    vẫn gửi lại đúng key cũ và payload cũ.
 * 3. Chỉ xóa key sau khi backend xác nhận thành công hoặc lỗi dứt điểm (422).
 */
@Singleton
class IdempotencyKeyManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "omnigo_idempotency_storage"
        const val SCOPE_BOOKING = "BOOKING"
        const val SCOPE_FOOD_ORDER = "FOOD_ORDER"
        const val SCOPE_PAYMENT_CREATE = "PAYMENT_CREATE"

        private const val KEY_PREFIX_UUID = "key_"
        private const val KEY_PREFIX_FINGERPRINT = "fp_"
    }

    /**
     * Lấy key hiện có hoặc sinh mới nếu chưa tồn tại cho scope + fingerprint này.
     * Nếu cùng scope nhưng fingerprint khác (user đổi giỏ hàng/số tiền):
     * Sinh key mới cho ý định mới.
     */
    @Synchronized
    fun getOrCreateKey(scope: String, fingerprint: String): String {
        val savedFp = prefs.getString(KEY_PREFIX_FINGERPRINT + scope, null)
        val savedKey = prefs.getString(KEY_PREFIX_UUID + scope, null)

        return if (!savedKey.isNullOrBlank() && savedFp == fingerprint) {
            savedKey
        } else {
            val newKey = UUID.randomUUID().toString()
            prefs.edit()
                .putString(KEY_PREFIX_UUID + scope, newKey)
                .putString(KEY_PREFIX_FINGERPRINT + scope, fingerprint)
                .apply()
            newKey
        }
    }

    /**
     * Lấy key đang hoạt động của scope nếu có.
     */
    @Synchronized
    fun getActiveKey(scope: String): String? {
        return prefs.getString(KEY_PREFIX_UUID + scope, null)
    }

    /**
     * Xóa key sau khi backend đã hoàn tất xử lý (200 OK thành công hoặc 422 Unprocessable).
     * Tuyệt đối không xóa key khi gặp 409 Conflict hoặc Timeout/5xx.
     */
    @Synchronized
    fun clearKey(scope: String) {
        prefs.edit()
            .remove(KEY_PREFIX_UUID + scope)
            .remove(KEY_PREFIX_FINGERPRINT + scope)
            .apply()
    }

    /**
     * Xóa toàn bộ key lưu trữ (khi đăng xuất).
     */
    @Synchronized
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

