package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15

@Composable
fun OrderDetailRestaurantAndItemsSection(
    restaurantName: String,
    restaurantAddress: String?,
    items: List<FoodOrderItem>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppSpacing.XXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            // Restaurant info header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.S))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = restaurantName.ifBlank { stringResource(id = R.string.order_detail_restaurant_title) },
                        style = MaterialTheme.typography.s15.bold(),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!restaurantAddress.isNullOrBlank()) {
                        Text(
                            text = restaurantAddress,
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(AppSpacing.M))

            Text(
                text = stringResource(id = R.string.order_detail_items_title, items.size),
                style = MaterialTheme.typography.s14.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.XXS),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                id = R.string.order_detail_item_quantity_format,
                                item.quantity,
                                item.itemName
                            ),
                            style = MaterialTheme.typography.s14.medium(),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.note.isNotBlank()) {
                            Text(
                                text = stringResource(
                                    id = R.string.order_detail_item_note_format,
                                    item.note
                                ),
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextSecondary
                            )
                        }
                    }

                    Text(
                        text = formatFoodOrderPrice(item.subtotal),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
