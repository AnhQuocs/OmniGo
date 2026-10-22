package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import java.util.Locale

@Composable
fun OrderDetailStatusBanner(
    status: String,
    modifier: Modifier = Modifier
) {
    val upperStatus = status.uppercase(Locale.ROOT)
    val isDelivered = upperStatus in setOf("DELIVERED", "COMPLETED")
    val isCancelled = upperStatus in setOf("CANCELLED", "REJECTED")

    val containerColor = when {
        isDelivered -> SuccessColor.copy(alpha = 0.12f)
        isCancelled -> ErrorColor.copy(alpha = 0.10f)
        upperStatus == "DELIVERING" -> PrimaryColor.copy(alpha = 0.10f)
        else -> SecondaryColor.copy(alpha = 0.10f)
    }

    val iconBgColor = when {
        isDelivered -> SuccessColor
        isCancelled -> ErrorColor
        upperStatus == "DELIVERING" -> PrimaryColor
        else -> SecondaryColor
    }

    val iconVector = when {
        isDelivered -> Icons.Default.Check
        isCancelled -> Icons.Default.Cancel
        upperStatus == "DELIVERING" -> Icons.Default.DeliveryDining
        upperStatus == "PREPARING" -> Icons.Default.Fastfood
        else -> Icons.Default.HourglassTop
    }

    val titleRes = when {
        isDelivered -> R.string.order_detail_status_delivered_banner
        isCancelled -> R.string.order_detail_status_cancelled
        upperStatus == "DELIVERING" -> R.string.order_detail_status_delivering
        upperStatus == "PREPARING" -> R.string.order_detail_status_preparing
        else -> R.string.order_detail_status_pending
    }

    val subtitleRes = when {
        isDelivered -> R.string.order_detail_status_delivered_subtitle
        isCancelled -> R.string.order_detail_status_cancelled_desc
        upperStatus == "DELIVERING" -> R.string.order_detail_status_delivering_desc
        upperStatus == "PREPARING" -> R.string.order_detail_status_preparing_desc
        else -> R.string.order_detail_status_pending_desc
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingML, vertical = Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeL)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.M))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = titleRes),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = stringResource(id = subtitleRes),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(Dimen.SizeSM)
            )
        }
    }
}
