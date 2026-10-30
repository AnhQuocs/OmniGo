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
import androidx.compose.ui.text.style.TextDecoration
import com.example.omnigo.R
import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DepositWalletTabContent(
    depositBalance: Double,
    transactions: List<DriverWalletTransaction>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Balance Overview Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppShape.ShapeL))
                .background(if (depositBalance < 0) ErrorColor.copy(alpha = 0.08f) else SecondaryContainer.copy(alpha = 0.4f))
                .border(
                    Dimen.BorderHairline,
                    if (depositBalance < 0) ErrorColor.copy(alpha = 0.2f) else CardBorderColor,
                    RoundedCornerShape(AppShape.ShapeL)
                )
                .padding(Dimen.PaddingM)
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.driver_wallet_deposit_subtitle),
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = String.format(Locale.getDefault(), "%,.0f ₫", depositBalance),
                    style = MaterialTheme.typography.s20.bold(),
                    color = if (depositBalance < 0) ErrorColor else SecondaryColor
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
                        text = stringResource(id = R.string.driver_wallet_note_success_only),
                        style = MaterialTheme.typography.s10.normal(),
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.M))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.driver_wallet_history_empty),
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
                itemsIndexed(transactions, key = { idx, tx -> "${tx.id}_$idx" }) { index, tx ->
                    val isPending = tx.status == "PENDING"
                    val isFailed = tx.status == "FAILED" || tx.status == "CANCELED"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.description,
                                style = MaterialTheme.typography.s13.semiBold(),
                                color = if (isPending) TextSecondary else TextPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.XXS))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS)
                            ) {
                                Text(
                                    text = tx.timeFormatted,
                                    style = MaterialTheme.typography.s10.normal(),
                                    color = TextTertiary
                                )
                                TransactionBadge(status = tx.status, label = tx.statusLabel)
                            }
                        }

                        Text(
                            text = tx.amountFormatted,
                            style = MaterialTheme.typography.s14.bold(),
                            textDecoration = if (isFailed) TextDecoration.LineThrough else TextDecoration.None,
                            color = when {
                                isFailed -> TextTertiary
                                isPending -> SecondaryColor
                                tx.isIncome -> PrimaryColor
                                else -> SecondaryColor
                            }
                        )
                    }

                    if (index < transactions.size - 1) {
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

@Composable
fun TransactionBadge(status: String, label: String) {
    val (bg, textColor) = when (status) {
        "PENDING" -> SecondaryContainer to SecondaryColor
        "SUCCESS" -> PrimaryContainer to PrimaryColor
        "FAILED", "CANCELED" -> ErrorColor.copy(alpha = 0.12f) to ErrorColor
        else -> SecondaryContainer to TextSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(AppShape.PillShape))
            .background(bg)
            .padding(horizontal = Dimen.PaddingXSPlus, vertical = Dimen.PaddingXXS)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.s10.medium(),
            color = textColor
        )
    }
}

