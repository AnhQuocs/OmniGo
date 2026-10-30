package com.example.omnigo.features.driver.earnings.presentation.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.earnings.domain.util.EarningsTripItem
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun EarningsTripDetailSection(
    tripItems: List<EarningsTripItem>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXS),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            // Header Row: Rounded square icon + Title + Chevron >
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeWalletBadge)
                            .clip(RoundedCornerShape(AppShape.ShapeM))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.driver_earnings_detail_today),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(Dimen.SizeS)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            if (tripItems.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
                ) {
                    tripItems.forEachIndexed { index, item ->
                        TripDetailItemRow(item = item)
                        if (index < tripItems.size - 1) {
                            HorizontalDivider(
                                color = OutlineVariantLight.copy(alpha = 0.5f),
                                thickness = Dimen.PaddingXXS
                            )
                        }
                    }
                }
            } else {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.PaddingM),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeWalletBadge)
                            .clip(CircleShape)
                            .background(PrimaryContainer.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = PrimaryColor.copy(alpha = 0.45f),
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.S))

                    Text(
                        text = stringResource(id = R.string.driver_earnings_trips_empty_title),
                        style = MaterialTheme.typography.s13.semiBold(),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.XXS))

                    Text(
                        text = stringResource(id = R.string.driver_earnings_trips_empty_subtitle),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun TripDetailItemRow(
    item: EarningsTripItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.PaddingXXS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(AppSpacing.XXS))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS)
            ) {
                Text(
                    text = item.time,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextTertiary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.XXS)
                ) {
                    Icon(
                        imageVector = if (item.isCash) Icons.Default.Payments else Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = if (item.isCash) SecondaryColor else PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeIconXS)
                    )
                    Text(
                        text = if (item.isCash) "Tiền mặt" else "Ví ứng dụng",
                        style = MaterialTheme.typography.s10.normal(),
                        color = if (item.isCash) SecondaryColor else PrimaryColor
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format(Locale.getDefault(), "+%,.0f ₫", item.netAmount),
                style = MaterialTheme.typography.s15.bold(),
                color = PrimaryColor
            )
            Spacer(modifier = Modifier.height(AppSpacing.XXS))
            Text(
                text = String.format(Locale.getDefault(), "Cước: %,.0f ₫", item.grossAmount),
                style = MaterialTheme.typography.s10.normal(),
                color = TextTertiary
            )
        }
    }
}
