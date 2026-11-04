package com.example.omnigo.features.customer.food.presentation.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.presentation.format.formatFoodPrice
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickTopUpBottomSheet(
    deficitAmount: Double,
    onConfirmTopUp: (Double) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Calculate suggested amount: round up to nearest 10,000, minimum 10,000
    val roundedDeficit = remember(deficitAmount) {
        val raw = if (deficitAmount < 10000.0) 10000.0 else ceil(deficitAmount / 10000.0) * 10000.0
        raw
    }

    val standardOptions = remember(roundedDeficit) {
        listOf(50000.0, 100000.0, 200000.0, 500000.0).filter { it > roundedDeficit }
    }

    var selectedAmount by remember(roundedDeficit) { mutableDoubleStateOf(roundedDeficit) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundLight,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingL)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(id = R.string.checkout_quick_top_up_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.XXS))

            Text(
                text = stringResource(id = R.string.checkout_quick_top_up_subtitle),
                style = MaterialTheme.typography.s14.normal(),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // Deficit Option Item (Recommended)
            TopUpOptionCard(
                title = stringResource(
                    id = R.string.checkout_quick_top_up_deficit_option,
                    formatFoodPrice(roundedDeficit)
                ),
                amount = roundedDeficit,
                isSelected = selectedAmount == roundedDeficit,
                onClick = { selectedAmount = roundedDeficit }
            )

            if (standardOptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppSpacing.M))

                Text(
                    text = stringResource(id = R.string.checkout_quick_top_up_custom_amount),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(AppSpacing.S))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.S),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    standardOptions.forEach { amount ->
                        val isSelected = selectedAmount == amount
                        val borderColor = if (isSelected) PrimaryColor else MaterialTheme.colorScheme.outlineVariant
                        val containerColor = if (isSelected) PrimaryColor.copy(alpha = 0.08f) else SurfaceLight

                        Surface(
                            onClick = { selectedAmount = amount },
                            shape = RoundedCornerShape(AppShape.ShapeM),
                            color = containerColor,
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Text(
                                text = formatFoodPrice(amount),
                                style = if (isSelected) MaterialTheme.typography.s14.bold() else MaterialTheme.typography.s14.normal(),
                                color = if (isSelected) PrimaryColor else TextPrimary,
                                modifier = Modifier.padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.XL))

            Button(
                onClick = { onConfirmTopUp(selectedAmount) },
                enabled = !isLoading && selectedAmount >= 10000.0,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = SurfaceLight,
                        modifier = Modifier.size(Dimen.SizeM),
                        strokeWidth = AppSpacing.XXS
                    )
                } else {
                    Text(
                        text = "${stringResource(id = R.string.checkout_quick_top_up_btn)} • ${formatFoodPrice(selectedAmount)}",
                        style = MaterialTheme.typography.s16.bold(),
                        color = SurfaceLight
                    )
                }
            }
        }
    }
}

@Composable
private fun TopUpOptionCard(
    title: String,
    amount: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) PrimaryColor else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) PrimaryColor.copy(alpha = 0.08f) else SurfaceLight

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppShape.ShapeM),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = Dimen.PaddingS)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.s15.semiBold(),
                    color = if (isSelected) PrimaryColor else TextPrimary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = stringResource(id = R.string.checkout_payment_vnpay),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
            }
            Text(
                text = formatFoodPrice(amount),
                style = MaterialTheme.typography.s16.bold(),
                color = if (isSelected) PrimaryColor else TextPrimary
            )
        }
    }
}

