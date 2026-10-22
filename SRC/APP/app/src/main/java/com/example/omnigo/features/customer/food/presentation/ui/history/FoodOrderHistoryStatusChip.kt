package com.example.omnigo.features.customer.food.presentation.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s12
import java.util.Locale

@Composable
fun FoodOrderHistoryStatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val config = getStatusChipConfig(status)

    Box(
        modifier = modifier
            .background(color = config.bg, shape = RoundedCornerShape(AppShape.ShapeXL2))
            .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
    ) {
        Text(
            text = stringResource(config.labelRes),
            style = MaterialTheme.typography.s12.bold(),
            color = config.fg
        )
    }
}

private fun getStatusChipConfig(status: String): StatusChipConfig {
    return when (status.uppercase(Locale.ROOT)) {
        "COMPLETED", "DELIVERED" -> StatusChipConfig(
            labelRes = R.string.order_detail_status_completed,
            bg = SuccessColor.copy(alpha = 0.15f),
            fg = SuccessColor
        )
        "DELIVERING", "PICKED_UP", "ARRIVED_CUSTOMER" -> StatusChipConfig(
            labelRes = R.string.order_detail_status_delivering,
            bg = PrimaryColor.copy(alpha = 0.15f),
            fg = PrimaryColor
        )
        "PREPARING", "ACCEPTED" -> StatusChipConfig(
            labelRes = R.string.order_detail_status_preparing,
            bg = SecondaryColor.copy(alpha = 0.18f),
            fg = SecondaryColor
        )
        "PENDING", "PENDING_MERCHANT" -> StatusChipConfig(
            labelRes = R.string.order_detail_status_pending,
            bg = SecondaryColor.copy(alpha = 0.18f),
            fg = SecondaryColor
        )
        "AWAITING_PAYMENT", "PENDING_PAYMENT" -> StatusChipConfig(
            labelRes = R.string.order_detail_status_awaiting_payment,
            bg = SecondaryColor.copy(alpha = 0.18f),
            fg = SecondaryColor
        )
        "CANCELLED" -> StatusChipConfig(
            labelRes = R.string.order_detail_status_cancelled,
            bg = Color(0xFFE9ECEF),
            fg = TextSecondary
        )
        "REJECTED" -> StatusChipConfig(
            labelRes = R.string.order_detail_status_rejected,
            bg = ErrorColor.copy(alpha = 0.12f),
            fg = ErrorColor
        )
        "NO_DRIVER_FOUND" -> StatusChipConfig(
            labelRes = R.string.order_detail_status_no_driver,
            bg = SecondaryColor.copy(alpha = 0.18f),
            fg = SecondaryColor
        )
        else -> StatusChipConfig(
            labelRes = R.string.order_detail_status_unknown,
            bg = Color(0xFFE9ECEF),
            fg = TextSecondary
        )
    }
}

private data class StatusChipConfig(
    val labelRes: Int,
    val bg: Color,
    val fg: Color
)
