package com.example.omnigo.features.customer.food.presentation.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R

/** Giá hiển thị thống nhất; dữ liệu không hợp lệ hiển thị là "không có thông tin". */
@Composable
fun formatFoodPrice(price: Double?): String {
    val amount = FoodPriceFormatter.formatAmount(price)
        ?: return stringResource(id = R.string.order_detail_value_unavailable)
    return stringResource(id = R.string.food_currency_vnd_format, amount)
}
