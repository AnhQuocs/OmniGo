package com.example.omnigo.utils

object PhoneUtils {

    private val VIETNAM_PHONE_REGEX = Regex("^(?:\\+84|0)(3|5|7|8|9)\\d{8}$")

    fun isValidVietnamesePhoneNumber(phone: String): Boolean {
        val trimmed = phone.trim().replace(" ", "").replace("-", "")
        return trimmed.matches(VIETNAM_PHONE_REGEX)
    }

    /**
     * Chuẩn hóa số điện thoại sang định dạng E.164 (+84...) cho Firebase Auth
     */
    fun toE164(phone: String): String {
        val cleaned = phone.trim().replace(" ", "").replace("-", "")
        return when {
            cleaned.startsWith("+84") -> cleaned
            cleaned.startsWith("84") -> "+$cleaned"
            cleaned.startsWith("0") -> "+84${cleaned.substring(1)}"
            else -> "+84$cleaned"
        }
    }

    /**
     * Chuẩn hóa số điện thoại sang định dạng nội địa (0...)
     */
    fun toLocal(phone: String): String {
        val cleaned = phone.trim().replace(" ", "").replace("-", "")
        return when {
            cleaned.startsWith("+84") -> "0${cleaned.substring(3)}"
            cleaned.startsWith("84") -> "0${cleaned.substring(2)}"
            cleaned.startsWith("0") -> cleaned
            else -> "0$cleaned"
        }
    }

    /**
     * So sánh 2 số điện thoại có cùng một người dùng hay không
     */
    fun isSamePhoneNumber(phone1: String?, phone2: String?): Boolean {
        if (phone1.isNullOrBlank() || phone2.isNullOrBlank()) return false
        return toE164(phone1) == toE164(phone2)
    }
}
