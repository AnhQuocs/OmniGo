package com.example.omnigo.features.customer.wallet.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.MintGreenContainer
import com.example.omnigo.ui.theme.MintGreenPrimary
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PeachOrangeContainer
import com.example.omnigo.ui.theme.PeachOrangePrimary
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun WalletTransactionCard(
    tx: DriverWalletTransaction,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationXS),
        modifier = modifier
            .fillMaxWidth()
            .border(Dimen.BorderHairline, CardBorderColor, RoundedCornerShape(AppShape.ShapeL))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXL)
                    .clip(CircleShape)
                    .background(if (tx.isIncome) MintGreenContainer else PeachOrangeContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tx.isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (tx.isIncome) MintGreenPrimary else PeachOrangePrimary,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = tx.timeFormatted,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }
            Text(
                text = tx.amountFormatted,
                style = MaterialTheme.typography.s14.bold(),
                color = if (tx.isIncome) MintGreenPrimary else ErrorColor
            )
        }
    }
}

@Composable
fun WalletAmountDialog(
    title: String,
    showMethodSelector: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf("50000") }
    var selectedMethod by remember { mutableStateOf("VNPAY") }
    val presets = listOf(50000L, 100000L, 200000L, 500000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.S)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text(stringResource(id = R.string.wallet_amount_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS)
                ) {
                    presets.forEach { preset ->
                        Card(
                            shape = RoundedCornerShape(AppShape.ShapeS),
                            colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationNone),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amountText = preset.toString() }
                        ) {
                            Text(
                                text = "${preset / 1000}k",
                                style = MaterialTheme.typography.s12.semiBold(),
                                color = PrimaryColor,
                                modifier = Modifier.padding(vertical = Dimen.PaddingXS, horizontal = Dimen.PaddingXS)
                            )
                        }
                    }
                }
                if (showMethodSelector) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)) {
                        listOf("VNPAY", "MOMO").forEach { method ->
                            val isSelected = selectedMethod == method
                            Card(
                                shape = RoundedCornerShape(AppShape.ShapeS),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) PrimaryColor else SurfaceLight
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationNone),
                                modifier = Modifier
                                    .border(Dimen.BorderHairline, PrimaryColor, RoundedCornerShape(AppShape.ShapeS))
                                    .clickable { selectedMethod = method }
                            ) {
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.s13.bold(),
                                    color = if (isSelected) OnPrimaryColor else PrimaryColor,
                                    modifier = Modifier.padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXS)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = amountText.toDoubleOrNull() ?: 0.0
                    if (value >= 10000.0) onConfirm(value, selectedMethod)
                },
                enabled = !isSubmitting
            ) {
                Text(
                    text = stringResource(id = android.R.string.ok),
                    style = MaterialTheme.typography.s14.bold(),
                    color = PrimaryColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = android.R.string.cancel),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary
                )
            }
        },
        shape = RoundedCornerShape(AppShape.ShapeL)
    )
}

