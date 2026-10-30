package com.example.omnigo.features.driver.earnings.presentation.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
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
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun IncomeWalletTabContent(
    incomeBalance: Double,
    tripItems: List<EarningsTripItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Income Overview Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppShape.ShapeL))
                .background(PrimaryContainer.copy(alpha = 0.5f))
                .border(Dimen.BorderHairline, CardBorderColor, RoundedCornerShape(AppShape.ShapeL))
                .padding(Dimen.PaddingM)
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.driver_wallet_income_subtitle),
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = String.format(Locale.getDefault(), "%,.0f ₫", incomeBalance),
                    style = MaterialTheme.typography.s20.bold(),
                    color = PrimaryColor
                )
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(Dimen.SizeS)
                    )
                    Text(
                        text = stringResource(id = R.string.driver_wallet_income_note),
                        style = MaterialTheme.typography.s10.normal(),
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        if (tripItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.driver_earnings_trips_empty_title),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
            ) {
                itemsIndexed(tripItems, key = { idx, item -> "${item.id}_$idx" }) { index, trip ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(Dimen.SizeL)
                                    .clip(RoundedCornerShape(AppShape.ShapeM))
                                    .background(PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(Dimen.SizeS)
                                )
                            }

                            Column {
                                Text(
                                    text = trip.title,
                                    style = MaterialTheme.typography.s13.semiBold(),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                                Text(
                                    text = "${trip.time} • ${if (trip.isCash) "Tiền mặt" else "Ví Omni"}",
                                    style = MaterialTheme.typography.s10.normal(),
                                    color = TextTertiary
                                )
                            }
                        }

                        Text(
                            text = String.format(Locale.getDefault(), "+%,.0f ₫", trip.netAmount),
                            style = MaterialTheme.typography.s14.bold(),
                            color = PrimaryColor
                        )
                    }

                    if (index < tripItems.size - 1) {
                        HorizontalDivider(
                            color = OutlineVariantLight.copy(alpha = 0.5f),
                            thickness = Dimen.BorderHairline
                        )
                    }
                }
            }
        }
    }
}

