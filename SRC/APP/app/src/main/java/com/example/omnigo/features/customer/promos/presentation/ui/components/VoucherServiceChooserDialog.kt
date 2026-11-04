package com.example.omnigo.features.customer.promos.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.example.omnigo.R
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun VoucherServiceChooserDialog(
    voucher: Voucher,
    onChooseRide: (String) -> Unit,
    onChooseFood: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppShape.ShapeXL),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationL)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.promos_choose_service_title),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(Dimen.SizeL)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(Dimen.SizeM)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                Text(
                    text = stringResource(id = R.string.promos_choose_service_desc, voucher.code),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Service Option 1: RIDE
                ServiceOptionCard(
                    title = stringResource(id = R.string.promos_choose_service_ride),
                    subtitle = stringResource(id = R.string.promos_filter_ride),
                    icon = Icons.Default.DirectionsCar,
                    gradient = Brush.linearGradient(listOf(Color(0xFF0066EE), Color(0xFF06B6D4))),
                    onClick = {
                        onDismiss()
                        onChooseRide(voucher.code)
                    }
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Service Option 2: FOOD
                ServiceOptionCard(
                    title = stringResource(id = R.string.promos_choose_service_food),
                    subtitle = stringResource(id = R.string.promos_filter_food),
                    icon = Icons.Default.Fastfood,
                    gradient = Brush.linearGradient(listOf(Color(0xFFFF7A00), Color(0xFFFF5252))),
                    onClick = {
                        onDismiss()
                        onChooseFood(voucher.code)
                    }
                )
            }
        }
    }
}

@Composable
private fun ServiceOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeL))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeXXL)
                        .clip(RoundedCornerShape(AppShape.PillShape))
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.M))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextWhite
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextWhite.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

