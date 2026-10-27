package com.example.omnigo.features.customer.food.presentation.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.presentation.format.formatFoodPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14

@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    quantityInCart: Int,
    canOrder: Boolean,
    onItemClick: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onRemoveFromCart: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = { onItemClick(menuItem) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeM),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppSpacing.XXS)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hình ảnh món
            Box(
                modifier = Modifier
                    .size(Dimen.SizeMega)
                    .clip(RoundedCornerShape(AppShape.ShapeS))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (menuItem.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(menuItem.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = menuItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (!menuItem.isAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TextPrimary.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.menu_item_sold_out),
                            style = MaterialTheme.typography.s10.bold(),
                            color = TextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            // Thông tin chi tiết món
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menuItem.name,
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (menuItem.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = menuItem.description,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                Text(
                    text = formatFoodPrice(menuItem.price),
                    style = MaterialTheme.typography.s14.bold(),
                    color = PrimaryColor
                )
            }

            // Nút Thêm / Bớt số lượng khi nhà hàng nhận đơn và món còn hàng
            if (canOrder && menuItem.isAvailable) {
                Spacer(modifier = Modifier.width(AppSpacing.S))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (quantityInCart > 0) {
                        Surface(
                            shape = RoundedCornerShape(AppShape.ShapeXXS),
                            color = SurfaceLight,
                            border = BorderStroke(Dimen.PaddingXXS, PrimaryColor)
                        ) {
                            IconButton(
                                onClick = { onRemoveFromCart(menuItem) },
                                modifier = Modifier.size(Dimen.SizeL)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = stringResource(
                                        id = R.string.food_cd_remove_item,
                                        menuItem.name
                                    ),
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(Dimen.SizeS)
                                )
                            }
                        }

                        Text(
                            text = quantityInCart.toString(),
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = AppSpacing.S)
                        )
                    }

                    IconButton(
                        onClick = { onAddToCart(menuItem) },
                        modifier = Modifier.size(Dimen.SizeL),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryColor)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(
                                id = R.string.food_cd_add_item,
                                menuItem.name
                            ),
                            tint = TextWhite,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                    }
                }
            }
        }
    }
}
