package com.example.omnigo.features.driver.food.presentation.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrderItem
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DeliveryOrderItemsCard(
    items: List<DriverDeliveryOrderItem>,
    note: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Text(
                text = stringResource(id = R.string.driver_delivery_items_title, items.size),
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        color = OutlineVariantLight.copy(alpha = 0.3f),
                        thickness = Dimen.PaddingXXS,
                        modifier = Modifier.padding(vertical = Dimen.PaddingXS)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.quantity}x ${item.itemName}",
                        style = MaterialTheme.typography.s13.medium(),
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = String.format(Locale.US, "%,.0f ₫", item.subtotal),
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextSecondary
                    )
                }
                if (!item.note.isNullOrBlank()) {
                    Text(
                        text = stringResource(id = R.string.driver_delivery_item_note, item.note),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }

            if (!note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.S))
                Text(
                    text = stringResource(id = R.string.driver_delivery_order_note, note),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextSecondary
                )
            }
        }
    }
}
