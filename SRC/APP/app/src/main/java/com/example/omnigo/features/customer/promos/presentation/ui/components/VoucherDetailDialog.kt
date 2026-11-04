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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.example.omnigo.R
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.model.VoucherDiscountType
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.formatVnd
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun VoucherDetailDialog(
    voucher: Voucher,
    onDismiss: () -> Unit,
    onUseClick: (Voucher) -> Unit,
    onCopyCode: (String) -> Unit
) {
    val isFood = voucher.applicableService.equals("FOOD", ignoreCase = true)
    val accentColor = if (isFood) SecondaryColor else PrimaryColor
    val badgeBg = if (isFood) SecondaryContainer else PrimaryContainer

    val serviceText = when (voucher.applicableService.uppercase()) {
        "FOOD" -> stringResource(id = R.string.promos_service_food)
        "RIDE" -> stringResource(id = R.string.promos_service_ride)
        else -> stringResource(id = R.string.promos_service_all)
    }

    val discountText = if (voucher.discountType == VoucherDiscountType.PERCENTAGE) {
        "${voucher.discountValue.toInt()}%"
    } else {
        voucher.discountValue.formatVnd()
    }

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
                // Header: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(Dimen.SizeL)
                                .clip(RoundedCornerShape(AppShape.ShapeM))
                                .background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = stringResource(id = R.string.promos_detail_title),
                            style = MaterialTheme.typography.s18.bold(),
                            color = TextPrimary
                        )
                    }

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

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Promo Code Copy Card
                Surface(
                    onClick = { onCopyCode(voucher.code) },
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    color = badgeBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.promos_detail_code),
                                style = MaterialTheme.typography.s12.medium(),
                                color = TextSecondary
                            )
                            Text(
                                text = voucher.code,
                                style = MaterialTheme.typography.s16.bold(),
                                color = accentColor
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Voucher Title & Description
                Text(
                    text = voucher.name.ifBlank { voucher.code },
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary
                )

                if (voucher.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.XS))
                    Text(
                        text = voucher.description,
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Detail Rows
                DetailInfoRow(
                    label = stringResource(id = R.string.promos_off_label),
                    value = discountText
                )

                if (voucher.minOrderAmount > 0) {
                    DetailInfoRow(
                        label = stringResource(id = R.string.promos_detail_min_order),
                        value = voucher.minOrderAmount.formatVnd()
                    )
                }

                if (voucher.maxDiscountAmount != null && voucher.maxDiscountAmount > 0) {
                    DetailInfoRow(
                        label = stringResource(id = R.string.promos_detail_max_discount),
                        value = voucher.maxDiscountAmount.formatVnd()
                    )
                }

                DetailInfoRow(
                    label = stringResource(id = R.string.promos_detail_service),
                    value = serviceText
                )

                if (!voucher.endDate.isNullOrBlank()) {
                    DetailInfoRow(
                        label = stringResource(id = R.string.promos_detail_validity),
                        value = voucher.endDate
                    )
                }

                if (voucher.perUserLimit > 0) {
                    DetailInfoRow(
                        label = stringResource(id = R.string.promos_detail_limit),
                        value = stringResource(id = R.string.promos_detail_limit_value, voucher.perUserLimit)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.SPlus)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimen.HeightDefault),
                        shape = RoundedCornerShape(AppShape.PillShape)
                    ) {
                        Text(
                            text = stringResource(id = R.string.promos_detail_close),
                            style = MaterialTheme.typography.s14.semiBold(),
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onUseClick(voucher)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(Dimen.HeightDefault),
                        shape = RoundedCornerShape(AppShape.PillShape),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text(
                            text = stringResource(id = R.string.promos_use_now),
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.PaddingXS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.s12.normal(),
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.s12.semiBold(),
            color = TextPrimary
        )
    }
}

