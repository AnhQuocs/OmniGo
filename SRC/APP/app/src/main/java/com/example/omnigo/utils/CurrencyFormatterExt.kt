package com.example.omnigo.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val vndFormatSymbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
    groupingSeparator = '.'
    decimalSeparator = ','
}

private val vndFormatter = DecimalFormat("#,###", vndFormatSymbols)

/**
 * Format Long amount to VND string with unit suffix (e.g. 150000 -> "150.000 đ")
 */
fun Long.formatVnd(): String {
    return "${vndFormatter.format(this)} đ"
}

/**
 * Format Double amount to VND string with unit suffix (e.g. 150000.0 -> "150.000 đ")
 */
fun Double.formatVnd(): String {
    return "${vndFormatter.format(this)} đ"
}

/**
 * Format Int amount to VND string with unit suffix
 */
fun Int.formatVnd(): String {
    return "${vndFormatter.format(this)} đ"
}

/**
 * Format BigDecimal amount to VND string with unit suffix
 */
fun BigDecimal?.formatVnd(): String {
    if (this == null) return "0 đ"
    return "${vndFormatter.format(this)} đ"
}

/**
 * Format Long amount to raw VND number without "đ" suffix (e.g. 150000 -> "150.000")
 */
fun Long.formatVndRaw(): String {
    return vndFormatter.format(this)
}

/**
 * Format Double amount to raw VND number without "đ" suffix (e.g. 150000.0 -> "150.000")
 */
fun Double.formatVndRaw(): String {
    return vndFormatter.format(this)
}

