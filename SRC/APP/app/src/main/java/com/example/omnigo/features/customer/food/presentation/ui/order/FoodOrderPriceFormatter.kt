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
