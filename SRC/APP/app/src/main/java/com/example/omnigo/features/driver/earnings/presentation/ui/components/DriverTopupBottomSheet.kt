package com.example.omnigo.features.driver.earnings.presentation.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverTopupBottomSheet(
    isSubmitting: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmTopup: (Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val denominations = listOf(100_000.0, 200_000.0, 500_000.0, 1_000_000.0)
    var selectedAmount by remember { mutableStateOf(200_000.0) }
    var selectedMethod by remember { mutableStateOf(0) } // 0: VietQR (VNPAY), 1: MoMo

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
        ) {
            Text(
                text = stringResource(id = R.string.driver_topup_sheet_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Text(
                text = stringResource(id = R.string.driver_topup_select_amount),
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Denomination Pills (2x2 Grid)
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.S)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                ) {
                    DenominationPill(
                        amount = denominations[0],
                        isSelected = selectedAmount == denominations[0],
                        onClick = { if (!isSubmitting) selectedAmount = denominations[0] },
                        modifier = Modifier.weight(1f)
                    )
                    DenominationPill(
                        amount = denominations[1],
                        isSelected = selectedAmount == denominations[1],
                        onClick = { if (!isSubmitting) selectedAmount = denominations[1] },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                ) {
                    DenominationPill(
                        amount = denominations[2],
                        isSelected = selectedAmount == denominations[2],
                        onClick = { if (!isSubmitting) selectedAmount = denominations[2] },
                        modifier = Modifier.weight(1f)
                    )
                    DenominationPill(
                        amount = denominations[3],
                        isSelected = selectedAmount == denominations[3],
                        onClick = { if (!isSubmitting) selectedAmount = denominations[3] },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.L))

            Text(
                text = stringResource(id = R.string.driver_topup_method_label),
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Method 1: VietQR
            PaymentMethodSelectRow(
                title = stringResource(id = R.string.driver_topup_method_qr),
                isSelected = selectedMethod == 0,
                onClick = { if (!isSubmitting) selectedMethod = 0 }
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Method 2: MoMo
            PaymentMethodSelectRow(
                title = stringResource(id = R.string.driver_topup_method_momo),
                isSelected = selectedMethod == 1,
                onClick = { if (!isSubmitting) selectedMethod = 1 }
            )

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // Confirm Button
            Button(
                onClick = {
                    val methodCode = if (selectedMethod == 0) "VNPAY" else "MOMO"
                    onConfirmTopup(selectedAmount, methodCode)
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(AppShape.PillShape),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = OnPrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM),
                        strokeWidth = AppSpacing.XXS
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.driver_topup_btn_confirm),
                        style = MaterialTheme.typography.s14.bold(),
                        color = OnPrimaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))
        }
    }
}

@Composable
private fun DenominationPill(
    amount: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) SecondaryContainer else SurfaceLight
    val border = if (isSelected) SecondaryColor else CardBorderColor
    val textCol = if (isSelected) SecondaryColor else TextPrimary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppShape.ShapeL))
            .background(bg)
            .border(Dimen.BorderHairline, border, RoundedCornerShape(AppShape.ShapeL))
            .clickable(onClick = onClick)
            .padding(vertical = Dimen.PaddingSM),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format(Locale.getDefault(), "%,.0f ₫", amount),
            style = if (isSelected) MaterialTheme.typography.s14.semiBold() else MaterialTheme.typography.s14.normal(),
            color = textCol
        )
    }
}

@Composable
private fun PaymentMethodSelectRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) SecondaryContainer.copy(alpha = 0.5f) else SurfaceLight
    val border = if (isSelected) SecondaryColor else CardBorderColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.ShapeM))
            .background(bg)
            .border(Dimen.BorderHairline, border, RoundedCornerShape(AppShape.ShapeM))
            .clickable(onClick = onClick)
            .padding(Dimen.PaddingSM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.s13.medium(),
            color = if (isSelected) SecondaryColor else TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}
