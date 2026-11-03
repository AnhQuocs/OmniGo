package com.example.omnigo.features.customer.promos.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.example.omnigo.R
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherSelectionBottomSheet(
    vouchers: List<Voucher>,
    orderAmount: Double,
    appliedVoucherCode: String?,
    isLoading: Boolean,
    onApplyCode: (String) -> Unit,
    onRemoveVoucher: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var manualCode by remember { mutableStateOf(appliedVoucherCode.orEmpty()) }
    var viewingVoucherDetail by remember { mutableStateOf<Voucher?>(null) }
    val clipboardManager = LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXXL, topEnd = AppShape.ShapeXXL)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM)
                .padding(bottom = Dimen.PaddingXL)
        ) {
            Text(
                text = stringResource(id = R.string.voucher_sheet_title),
                style = MaterialTheme.typography.s16.bold(),
                color = TextPrimary
            )
            Text(
                text = stringResource(id = R.string.voucher_sheet_subtitle),
                style = MaterialTheme.typography.s12.normal(),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it.uppercase() },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.promos_input_hint),
                            style = MaterialTheme.typography.s14.normal(),
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimen.HeightInputField),
                    shape = RoundedCornerShape(AppShape.PillShape),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = CardBorderColor,
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(AppSpacing.S))

                Button(
                    onClick = {
                        if (manualCode.isNotBlank()) {
                            onApplyCode(manualCode.trim())
                        }
                    },
                    enabled = manualCode.isNotBlank() && !isLoading,
                    modifier = Modifier.height(Dimen.HeightDefault),
                    shape = RoundedCornerShape(AppShape.PillShape),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text(
                        text = stringResource(id = R.string.promos_apply_btn),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.PaddingXL),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeL),
                        color = PrimaryColor,
                        strokeWidth = Dimen.PaddingXXS
                    )
                }
            } else if (vouchers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.PaddingL),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.promos_empty),
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.M),
                    contentPadding = PaddingValues(vertical = AppSpacing.XS)
                ) {
                    items(vouchers, key = { "${it.id}_${it.code}" }) { voucher ->
                        val isSelected = appliedVoucherCode != null &&
                            appliedVoucherCode.equals(voucher.code, ignoreCase = true)
                        VoucherItemCard(
                            voucher = voucher,
                            currentOrderSubtotal = orderAmount,
                            isSelected = isSelected,
                            onUseClick = {
                                if (isSelected) {
                                    onRemoveVoucher()
                                } else {
                                    onApplyCode(voucher.code)
                                }
                            },
                            onCopyCode = { code ->
                                clipboardManager.setText(AnnotatedString(code))
                            },
                            onDetailClick = { selected ->
                                viewingVoucherDetail = selected
                            }
                        )
                    }
                }
            }
        }
    }

    viewingVoucherDetail?.let { voucher ->
        VoucherDetailDialog(
            voucher = voucher,
            onDismiss = { viewingVoucherDetail = null },
            onUseClick = { selected ->
                viewingVoucherDetail = null
                onApplyCode(selected.code)
            },
            onCopyCode = { code ->
                clipboardManager.setText(AnnotatedString(code))
            }
        )
    }
}
