package com.example.omnigo.features.customer.food.presentation.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.domain.model.MenuItem
import com.example.omnigo.features.customer.food.presentation.format.formatFoodPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDetailBottomSheet(
    menuItem: MenuItem,
    quantity: Int,
    note: String,
    canOrder: Boolean,
    onQuantityChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onAddToCart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                .padding(bottom = Dimen.PaddingM)
        ) {
            // Close bar & title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingM),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (menuItem.category.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(AppShape.ShapeXS),
                        color = PrimaryContainer
                    ) {
                        Text(
                            text = menuItem.category,
                            style = MaterialTheme.typography.s12.medium(),
                            color = PrimaryColor,
                            modifier = Modifier.padding(horizontal = AppSpacing.S, vertical = AppSpacing.XXS)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(AppSpacing.XXS))
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(id = R.string.food_cd_close),
                        tint = TextSecondary
                    )
                }
            }

            // Image
            if (menuItem.imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightHeroBanner)
                        .padding(horizontal = Dimen.PaddingM)
                        .clip(RoundedCornerShape(AppShape.ShapeL))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(menuItem.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = menuItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(Dimen.HeightHeroBanner)
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.M))
            }

            // Info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingM)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = menuItem.name,
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.S))
                    Text(
                        text = formatFoodPrice(menuItem.price),
                        style = MaterialTheme.typography.s16.bold(),
                        color = PrimaryColor
                    )
                }

                if (menuItem.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.S))
                    Text(
                        text = menuItem.description,
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Quantity selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.menu_item_sheet_quantity),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(AppShape.ShapeS),
                            color = BackgroundLight
                        ) {
                            IconButton(
                                onClick = { onQuantityChange(-1) },
                                enabled = quantity > 1,
                                modifier = Modifier.size(Dimen.SizeL)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = stringResource(id = R.string.food_cd_decrease_quantity),
                                    tint = if (quantity > 1) TextPrimary else TextSecondary,
                                    modifier = Modifier.size(Dimen.SizeS)
                                )
                            }
                        }

                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.s16.bold(),
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = AppSpacing.XSPlus)
                        )

                        Surface(
                            shape = RoundedCornerShape(AppShape.ShapeS),
                            color = BackgroundLight
                        ) {
                            IconButton(
                                onClick = { onQuantityChange(1) },
                                modifier = Modifier.size(Dimen.SizeL)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = stringResource(id = R.string.food_cd_increase_quantity),
                                    tint = TextPrimary,
                                    modifier = Modifier.size(Dimen.SizeS)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Note input
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.checkout_driver_note_hint),
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppShape.ShapeS),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Add to cart CTA
                val totalItemAmount = menuItem.price * quantity
                val isEnabled = canOrder && menuItem.isAvailable

                Button(
                    onClick = onAddToCart,
                    enabled = isEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault),
                    shape = RoundedCornerShape(AppShape.ShapeXL),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    val label = when {
                        !canOrder -> stringResource(id = R.string.menu_item_sheet_blocked)
                        !menuItem.isAvailable -> stringResource(id = R.string.menu_item_sold_out)
                        else -> stringResource(id = R.string.menu_item_sheet_add, formatFoodPrice(totalItemAmount))
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextWhite
                    )
                }
            }
        }
    }
}
