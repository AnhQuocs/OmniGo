package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import java.util.Locale

@Composable
internal fun formatFoodOrderPrice(price: Double?): String =
    price?.let {
        stringResource(
            id = R.string.food_currency_vnd_format,
            String.format(Locale.US, "%,.0f", it)
        )
    } ?: stringResource(id = R.string.order_detail_value_unavailable)

fun formatFoodOrderDateTime(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return ""
    return try {
        val instant = java.time.Instant.parse(rawDate)
        val zone = java.time.ZoneId.systemDefault()
        val zdt = instant.atZone(zone)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm", Locale.getDefault())
        zdt.format(formatter)
    } catch (e: Exception) {
        try {
            val ldt = java.time.LocalDateTime.parse(rawDate)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm", Locale.getDefault())
            ldt.format(formatter)
        } catch (e2: Exception) {
            rawDate
        }
    }
}

fun formatFoodOrderTimeOnly(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return ""
    return try {
        val instant = java.time.Instant.parse(rawDate)
        val zone = java.time.ZoneId.systemDefault()
        val zdt = instant.atZone(zone)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        zdt.format(formatter)
    } catch (e: Exception) {
        try {
            val ldt = java.time.LocalDateTime.parse(rawDate)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
            ldt.format(formatter)
        } catch (e2: Exception) {
            rawDate
        }
    }
}

