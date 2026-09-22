package com.example.omnigo.features.customer.food.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import java.util.Locale

@Composable
fun CartBottomBar(
    totalQuantity: Int,
    totalAmount: Double,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = totalQuantity > 0,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
            shape = RoundedCornerShape(AppShape.ShapeXL),
            color = SurfaceLight,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cart Icon with Badge and Total Amount
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeXLPlus)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = "Cart",
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )

                        Surface(
                            shape = CircleShape,
                            color = PrimaryColor,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = totalQuantity.toString(),
                                    style = MaterialTheme.typography.s10.bold(),
                                    color = TextWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.M))

                    Column {
                        Text(
                            text = String.format(Locale.US, "%,.0f ₫", totalAmount),
                            style = MaterialTheme.typography.s16.bold(),
                            color = PrimaryColor
                        )
                        Text(
                            text = stringResource(
                                id = R.string.restaurant_detail_cart_summary,
                                totalQuantity,
                                String.format(Locale.US, "%,.0f ₫", totalAmount)
                            ),
                            style = MaterialTheme.typography.s12.medium(),
                            color = TextPrimary
                        )
                    }
                }

                // Checkout button
                Button(
                    onClick = onCheckoutClick,
                    shape = RoundedCornerShape(AppShape.ShapeXL),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.height(Dimen.HeightDefault)
                ) {
                    Text(
                        text = stringResource(id = R.string.restaurant_detail_cart_btn_checkout),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                }
            }
        }
    }
}
