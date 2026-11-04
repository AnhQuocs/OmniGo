package com.example.omnigo.features.customer.food.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.omnigo.features.customer.food.domain.model.CartItem
import com.example.omnigo.features.customer.food.presentation.format.formatFoodPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.italic
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    restaurantName: String,
    items: List<CartItem>,
    totalAmount: Double,
    onUpdateQuantity: (menuItemId: Long, quantity: Int) -> Unit,
    onClearCart: () -> Unit,
    onCheckoutClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    restaurantId: Long? = null,
    onNavigateToRestaurant: ((Long) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(items.isEmpty()) {
        if (items.isEmpty()) {
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXXL, topEnd = AppShape.ShapeXXL),
        containerColor = SurfaceLight,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            CartSheetHeader(
                restaurantName = restaurantName,
                restaurantId = restaurantId,
                onNavigateToRestaurant = onNavigateToRestaurant,
                onClearCart = onClearCart,
                onDismiss = onDismiss
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = Dimen.PaddingS),
                color = CardBorderColor
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(horizontal = Dimen.PaddingM),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
            ) {
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.XS))
                }

                items(items, key = { it.menuItem.id }) { item ->
                    CartSheetItemRow(
                        item = item,
                        onUpdateQuantity = onUpdateQuantity
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(AppSpacing.S))
                }
            }

            HorizontalDivider(color = CardBorderColor)

            CartSheetFooter(
                totalAmount = totalAmount,
                onCheckoutClick = {
                    onDismiss()
                    onCheckoutClick()
                }
            )
        }
    }
}

@Composable
private fun CartSheetHeader(
    restaurantName: String,
    restaurantId: Long?,
    onNavigateToRestaurant: ((Long) -> Unit)?,
    onClearCart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXLPlus)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeSM)
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            Column {
                Text(
                    text = restaurantName.ifBlank { stringResource(id = R.string.food_cart_sheet_title) },
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (onNavigateToRestaurant != null && restaurantId != null) {
                    Text(
                        text = stringResource(id = R.string.food_cart_sheet_view_restaurant),
                        style = MaterialTheme.typography.s12.medium(),
                        color = PrimaryColor,
                        modifier = Modifier.clickable {
                            onDismiss()
                            onNavigateToRestaurant(restaurantId)
                        }
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = onClearCart,
                contentPadding = PaddingValues(horizontal = Dimen.PaddingS)
            ) {
                Text(
                    text = stringResource(id = R.string.food_cart_sheet_clear_all),
                    style = MaterialTheme.typography.s13.bold(),
                    color = ErrorColor
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.food_cd_close_cart),
                    tint = TextSecondary,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            }
        }
    }
}

@Composable
private fun CartSheetItemRow(
    item: CartItem,
    onUpdateQuantity: (menuItemId: Long, quantity: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.XS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.menuItem.imageUrl)
                .crossfade(true)
                .error(R.drawable.ic_launcher_foreground)
                .placeholder(R.drawable.ic_launcher_foreground)
                .build(),
            contentDescription = item.menuItem.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(Dimen.SizeXXL)
                .clip(RoundedCornerShape(AppShape.ShapeM))
                .background(BackgroundLight)
        )

        Spacer(modifier = Modifier.width(AppSpacing.M))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.menuItem.name,
                style = MaterialTheme.typography.s14.bold(),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (item.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = stringResource(id = R.string.food_cart_sheet_note_prefix, item.note),
                    style = MaterialTheme.typography.s12.medium().italic(),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            Text(
                text = formatFoodPrice(item.menuItem.price),
                style = MaterialTheme.typography.s14.bold(),
                color = PrimaryColor
            )
        }

        Spacer(modifier = Modifier.width(AppSpacing.S))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(AppShape.ShapeXXS),
                color = SurfaceLight,
                border = BorderStroke(
                    Dimen.PaddingXXS,
                    if (item.quantity == 1) ErrorColor else PrimaryColor
                )
            ) {
                IconButton(
                    onClick = { onUpdateQuantity(item.menuItem.id, item.quantity - 1) },
                    modifier = Modifier.size(Dimen.SizeL)
                ) {
                    Icon(
                        imageVector = if (item.quantity == 1) Icons.Filled.DeleteOutline else Icons.Filled.Remove,
                        contentDescription = null,
                        tint = if (item.quantity == 1) ErrorColor else PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }
            }

            Text(
                text = item.quantity.toString(),
                style = MaterialTheme.typography.s14.bold(),
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = AppSpacing.S)
            )

            IconButton(
                onClick = { onUpdateQuantity(item.menuItem.id, item.quantity + 1) },
                modifier = Modifier.size(Dimen.SizeL),
                colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryColor)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(Dimen.SizeS)
                )
            }
        }
    }
}

@Composable
private fun CartSheetFooter(
    totalAmount: Double,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.food_cart_sheet_total),
                style = MaterialTheme.typography.s14.medium(),
                color = TextSecondary
            )
            Text(
                text = formatFoodPrice(totalAmount),
                style = MaterialTheme.typography.s18.bold(),
                color = PrimaryColor
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        Button(
            onClick = onCheckoutClick,
            shape = RoundedCornerShape(AppShape.ShapeXL),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.HeightDefault)
        ) {
            Text(
                text = stringResource(id = R.string.food_cart_sheet_checkout),
                style = MaterialTheme.typography.s16.bold(),
                color = TextWhite
            )
        }
    }
}

