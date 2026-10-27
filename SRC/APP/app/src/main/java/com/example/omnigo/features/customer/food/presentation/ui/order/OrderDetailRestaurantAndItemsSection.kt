package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14

@Composable
fun OrderDetailRestaurantAndItemsSection(
    items: List<FoodOrderItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXXS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingML)
        ) {
            // Header: Bag icon + "Danh sách món (3)"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(Dimen.SizeM)
                )

                Spacer(modifier = Modifier.width(AppSpacing.S))

                Text(
                    text = stringResource(id = R.string.order_detail_items_section_title, items.size),
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Items List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
            ) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dish Thumbnail Image
                        Box(
                            modifier = Modifier
                                .size(Dimen.HeightDefault)
                                .clip(RoundedCornerShape(AppShape.ShapeS))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!item.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.itemName,
                                    contentScale = ContentScale.Crop,
                                    placeholder = rememberVectorPainter(Icons.Default.Fastfood),
                                    error = rememberVectorPainter(Icons.Default.Fastfood),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Fastfood,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(Dimen.SizeM)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(AppSpacing.M))

                        // Name + Portion count + note
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.itemName,
                                style = MaterialTheme.typography.s14.bold(),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.XXS))

                            Text(
                                text = stringResource(id = R.string.order_detail_item_portion, item.quantity),
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextSecondary
                            )

                            if (item.note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                                Text(
                                    text = stringResource(id = R.string.order_detail_item_note_format, item.note),
                                    style = MaterialTheme.typography.s12.normal(),
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(AppSpacing.S))

                        // Price
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
}
