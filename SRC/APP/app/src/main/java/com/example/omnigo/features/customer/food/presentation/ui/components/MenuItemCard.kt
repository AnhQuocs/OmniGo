package com.example.omnigo.features.customer.food.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import java.util.Locale

@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    quantityInCart: Int,
    onAddToCart: (MenuItem) -> Unit,
    onRemoveFromCart: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAvailable = menuItem.isAvailable
    val cardAlpha = if (isAvailable) 1f else 0.6f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = SurfaceLight,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Image
            Box(
                modifier = Modifier
                    .size(Dimen.SizeMenuItemImage)
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(menuItem.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = menuItem.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(Dimen.SizeMenuItemImage)
                )

                if (!isAvailable) {
                    Surface(
                        shape = RoundedCornerShape(AppShape.ShapeXXS),
                        color = ErrorColor,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = AppSpacing.XXS)
                    ) {
                        Text(
                            text = stringResource(id = R.string.restaurant_detail_item_unavailable),
                            style = MaterialTheme.typography.s10.bold(),
                            color = TextWhite,
                            modifier = Modifier.padding(horizontal = AppSpacing.XS, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            // Item Details & Controls
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = menuItem.name,
                    style = MaterialTheme.typography.s15.bold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (menuItem.description.isNotBlank()) {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(Locale.US, "%,.0f ₫", menuItem.price),
                        style = MaterialTheme.typography.s14.bold(),
                        color = PrimaryColor
                    )

                    if (isAvailable) {
                        if (quantityInCart > 0) {
                            // Quantity selector (- count +)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AppShape.ShapeXL))
                                    .border(1.dp, CardBorderColor, RoundedCornerShape(AppShape.ShapeXL))
                                    .background(SurfaceLight)
                                    .padding(horizontal = AppSpacing.XXS, vertical = AppSpacing.XXS)
                            ) {
                                IconButton(
                                    onClick = { onRemoveFromCart(menuItem) },
                                    modifier = Modifier.size(Dimen.SizeML)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Remove,
                                        contentDescription = "Remove",
                                        tint = PrimaryColor,
                                        modifier = Modifier.size(Dimen.SizeS)
                                    )
                                }

                                Text(
                                    text = quantityInCart.toString(),
                                    style = MaterialTheme.typography.s14.bold(),
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = AppSpacing.S)
                                )

                                IconButton(
                                    onClick = { onAddToCart(menuItem) },
                                    modifier = Modifier.size(Dimen.SizeML)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add",
                                        tint = PrimaryColor,
                                        modifier = Modifier.size(Dimen.SizeS)
                                    )
                                }
                            }
                        } else {
                            // Add button
                            Button(
                                onClick = { onAddToCart(menuItem) },
                                shape = RoundedCornerShape(AppShape.ShapeXL),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = TextWhite,
                                    modifier = Modifier.size(Dimen.SizeS)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.XXS))
                                Text(
                                    text = stringResource(id = R.string.restaurant_detail_add_to_cart),
                                    style = MaterialTheme.typography.s12.bold(),
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
