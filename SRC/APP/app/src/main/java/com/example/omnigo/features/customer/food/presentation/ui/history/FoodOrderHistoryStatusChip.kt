package com.example.omnigo.features.customer.food.presentation.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.util.FoodOrderHistoryHelper
import com.example.omnigo.features.customer.food.domain.util.OrderHistoryStatusGroup
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12

@Composable
fun FoodOrderHistoryStatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val config = getStatusChipConfig(status)

    Row(
        modifier = modifier
            .background(color = config.bg, shape = CircleShape)
            .padding(horizontal = Dimen.PaddingXSPlus, vertical = Dimen.PaddingXXS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = null,
            tint = config.fg,
            modifier = Modifier.size(Dimen.SizeIconXS)
        )
        Spacer(modifier = Modifier.width(AppSpacing.XXS))
        Text(
            text = stringResource(config.labelRes),
            style = MaterialTheme.typography.s10.bold(),
            color = config.fg,
            maxLines = 1
        )
    }
}

@Composable
fun FoodOrderPaymentMethodChip(
    paymentMethod: PaymentMethod?,
    modifier: Modifier = Modifier
) {
    val config = getPaymentChipConfig(paymentMethod)

    Row(
        modifier = modifier
            .background(
                color = config.bg,
                shape = RoundedCornerShape(AppShape.ShapeXS)
            )
            .padding(horizontal = Dimen.PaddingXSPlus, vertical = Dimen.PaddingXXS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = null,
            tint = config.fg,
            modifier = Modifier.size(Dimen.SizeIconXS)
        )
        Spacer(modifier = Modifier.width(AppSpacing.XXS))
        Text(
            text = stringResource(config.labelRes),
            style = MaterialTheme.typography.s10.medium(),
            color = config.fg,
            maxLines = 1
        )
    }
}

private fun getStatusChipConfig(status: String): StatusChipConfig {
    return when (FoodOrderHistoryHelper.resolveStatusGroup(status)) {
        OrderHistoryStatusGroup.DELIVERED -> StatusChipConfig(
            labelRes = R.string.food_history_status_delivered,
            icon = Icons.Filled.CheckCircle,
            bg = SuccessColor.copy(alpha = 0.14f),
            fg = SuccessColor
        )
        OrderHistoryStatusGroup.CANCELLED -> StatusChipConfig(
            labelRes = R.string.food_history_status_cancelled,
            icon = Icons.Filled.Cancel,
            bg = ErrorContainer,
            fg = ErrorColor
        )
        OrderHistoryStatusGroup.ONGOING -> StatusChipConfig(
            labelRes = R.string.food_history_status_delivering,
            icon = Icons.Filled.TwoWheeler,
            bg = SecondaryContainer,
            fg = SecondaryColor
        )
    }
}

private fun getPaymentChipConfig(paymentMethod: PaymentMethod?): StatusChipConfig {
    return when (paymentMethod) {
        PaymentMethod.WALLET -> StatusChipConfig(
            labelRes = R.string.food_history_payment_method_wallet,
            icon = Icons.Outlined.CreditCard,
            bg = SecondaryContainer,
            fg = SecondaryColor
        )
        PaymentMethod.VNPAY -> StatusChipConfig(
            labelRes = R.string.food_history_payment_method_card,
            icon = Icons.Outlined.CreditCard,
            bg = SurfaceVariantLight,
            fg = TextSecondary
        )
        PaymentMethod.MOMO -> StatusChipConfig(
            labelRes = R.string.food_history_payment_method_momo,
            icon = Icons.Outlined.CreditCard,
            bg = SurfaceVariantLight,
            fg = TextSecondary
        )
        PaymentMethod.CASH, null -> StatusChipConfig(
            labelRes = R.string.food_history_payment_method_cash,
            icon = Icons.Outlined.AccountBalanceWallet,
            bg = SecondaryContainer,
            fg = SecondaryColor
        )
    }
}

private data class StatusChipConfig(
    val labelRes: Int,
    val icon: ImageVector,
    val bg: Color,
    val fg: Color
)
