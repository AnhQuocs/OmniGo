package com.example.omnigo.features.customer.promos.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.domain.model.VoucherDiscountType
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PeachOrangeContainer
import com.example.omnigo.ui.theme.PeachOrangePrimary
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.formatVnd
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s28
import com.example.omnigo.utils.semiBold

@Composable
fun VoucherItemCard(
    voucher: Voucher,
    currentOrderSubtotal: Double? = null,
    isSelected: Boolean = false,
    onUseClick: (Voucher) -> Unit,
    onCopyCode: ((String) -> Unit)? = null,
    onDetailClick: ((Voucher) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val service = voucher.applicableService.uppercase()
    val isFood = service == "FOOD"
    val isRide = service == "RIDE"

    val stubGradient = remember(service) {
        when {
            isFood -> Brush.verticalGradient(listOf(Color(0xFFFF7A00), Color(0xFFFF4848)))
            isRide -> Brush.verticalGradient(listOf(Color(0xFF0066EE), Color(0xFF06B6D4)))
            else -> Brush.verticalGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
        }
    }

    val serviceTag = when {
        isFood -> stringResource(id = R.string.promos_service_food)
        isRide -> stringResource(id = R.string.promos_service_ride)
        else -> stringResource(id = R.string.promos_service_all)
    }

    val (discountVal, discountUnit) = remember(voucher) {
        if (voucher.discountType == VoucherDiscountType.PERCENTAGE) {
            Pair(voucher.discountValue.toInt().toString(), "%")
        } else {
            val kValue = (voucher.discountValue / 1000).toInt()
            if (kValue > 0) Pair(kValue.toString(), "K")
            else Pair(voucher.discountValue.formatVnd(), "")
        }
    }

    val isIneligible = voucher.minOrderAmount > 0.0 &&
        currentOrderSubtotal != null &&
        currentOrderSubtotal < voucher.minOrderAmount

    val neededMore = if (isIneligible) {
        val curr = currentOrderSubtotal ?: 0.0
        (voucher.minOrderAmount - curr).coerceAtLeast(0.0)
    } else null

    val ticketShape = remember {
        TicketShape(
            cornerRadius = AppShape.ShapeL,
            notchRadius = Dimen.SizeTicketNotch,
            notchPositionX = Dimen.WidthTicketStub
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isIneligible) 0.62f else 1.0f),
        shape = ticketShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
        border = BorderStroke(
            if (isSelected) Dimen.BorderThin else Dimen.BorderHairline,
            if (isSelected) PrimaryColor else CardBorderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // LEFT STUB (Cuống vé độc lạ)
            Box(
                modifier = Modifier
                    .width(Dimen.WidthTicketStub)
                    .fillMaxHeight()
                    .background(stubGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.size(Dimen.SizeUltra)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = Dimen.PaddingXS, vertical = Dimen.PaddingS)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppShape.PillShape))
                            .background(Color.White.copy(alpha = 0.28f))
                            .padding(horizontal = Dimen.PaddingXSPlus, vertical = Dimen.PaddingXXS)
                    ) {
                        Text(
                            text = serviceTag,
                            style = MaterialTheme.typography.s10.bold(),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.XS))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = discountVal,
                            style = MaterialTheme.typography.s28.bold(),
                            color = Color.White
                        )
                        if (discountUnit.isNotEmpty()) {
                            Text(
                                text = discountUnit,
                                style = MaterialTheme.typography.s14.bold(),
                                color = Color.White,
                                modifier = Modifier.padding(bottom = Dimen.PaddingXXS)
                            )
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.promos_off_label),
                        style = MaterialTheme.typography.s10.semiBold(),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // PERFORATED SEPARATOR (Đường nét đứt xé vé)
            Canvas(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
            ) {
                val notchRPx = 10.dp.toPx()
                drawLine(
                    color = CardBorderColor,
                    start = Offset(0f, notchRPx),
                    end = Offset(0f, size.height - notchRPx),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            }

            // RIGHT BODY (Thân vé)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = Dimen.PaddingM,
                        top = Dimen.PaddingSM,
                        end = Dimen.PaddingSM,
                        bottom = Dimen.PaddingSM
                    )
            ) {
                // Row 1: Code pill badge + Limited Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        onClick = { onCopyCode?.invoke(voucher.code) },
                        shape = RoundedCornerShape(AppShape.ShapeXS),
                        color = if (isFood) SecondaryContainer else PrimaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = Dimen.PaddingXSPlus, vertical = Dimen.PaddingXXS)
                        ) {
                            Text(
                                text = voucher.code,
                                style = MaterialTheme.typography.s12.bold(),
                                color = if (isFood) SecondaryColor else PrimaryColor
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XXS))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = if (isFood) SecondaryColor else PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeIconXS)
                            )
                        }
                    }

                    if (voucher.usageLimit != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppShape.ShapeXXS))
                                .background(PeachOrangeContainer)
                                .padding(horizontal = Dimen.PaddingXS, vertical = Dimen.PaddingXXS)
                        ) {
                            Text(
                                text = stringResource(id = R.string.promos_tag_limited),
                                style = MaterialTheme.typography.s10.bold(),
                                color = PeachOrangePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                // Title
                Text(
                    text = voucher.name.ifBlank { voucher.code },
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Conditions
                val conditionText = buildString {
                    if (voucher.minOrderAmount > 0) {
                        append(stringResource(id = R.string.voucher_min_order_label, voucher.minOrderAmount.formatVnd()))
                    }
                    if (voucher.maxDiscountAmount != null && voucher.maxDiscountAmount > 0) {
                        if (isNotEmpty()) append(" • ")
                        append(stringResource(id = R.string.voucher_max_discount_label, voucher.maxDiscountAmount.formatVnd()))
                    }
                }

                if (conditionText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = conditionText,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }

                // Ineligible Warning
                if (neededMore != null) {
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = stringResource(id = R.string.voucher_min_order_warning, neededMore.formatVnd()),
                        style = MaterialTheme.typography.s10.medium(),
                        color = ErrorColor
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                // Bottom Row: Details link + Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onDetailClick?.invoke(voucher) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeIconXS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Text(
                            text = stringResource(id = R.string.promos_detail_btn),
                            style = MaterialTheme.typography.s12.medium(),
                            color = PrimaryColor
                        )
                    }

                    Button(
                        onClick = { onUseClick(voucher) },
                        enabled = !isIneligible,
                        shape = RoundedCornerShape(AppShape.PillShape),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) SecondaryColor else PrimaryColor,
                            disabledContainerColor = CardBorderColor
                        ),
                        contentPadding = PaddingValues(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXSPlus)
                    ) {
                        if (isSelected && currentOrderSubtotal != null) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(Dimen.SizeIconXS)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XXS))
                            Text(
                                text = stringResource(id = R.string.promos_selected_badge),
                                style = MaterialTheme.typography.s12.bold(),
                                color = TextWhite
                            )
                        } else {
                            val btnText = if (currentOrderSubtotal != null) {
                                stringResource(id = R.string.promos_btn_apply)
                            } else {
                                stringResource(id = R.string.promos_use_now)
                            }
                            Text(
                                text = btnText,
                                style = MaterialTheme.typography.s12.bold(),
                                color = if (isIneligible) TextSecondary else TextWhite
                            )
                        }
                    }
                }
            }
        }
    }
}
