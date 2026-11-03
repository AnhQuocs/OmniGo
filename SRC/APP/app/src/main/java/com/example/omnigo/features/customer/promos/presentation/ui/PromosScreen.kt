package com.example.omnigo.features.customer.promos.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.core.components.OmniGoMainLayout
import com.example.omnigo.features.customer.promos.domain.model.Voucher
import com.example.omnigo.features.customer.promos.presentation.ui.components.PromosEmptyState
import com.example.omnigo.features.customer.promos.presentation.ui.components.PromosFilterPills
import com.example.omnigo.features.customer.promos.presentation.ui.components.PromosHeroBanner
import com.example.omnigo.features.customer.promos.presentation.ui.components.PromosInputBar
import com.example.omnigo.features.customer.promos.presentation.ui.components.PromosTopBar
import com.example.omnigo.features.customer.promos.presentation.ui.components.VoucherDetailDialog
import com.example.omnigo.features.customer.promos.presentation.ui.components.VoucherItemCard
import com.example.omnigo.features.customer.promos.presentation.ui.components.VoucherServiceChooserDialog
import com.example.omnigo.features.customer.promos.presentation.ui.components.VoucherUsageHistorySheet
import com.example.omnigo.features.customer.promos.presentation.viewmodel.PromosViewModel
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor

@Composable
fun PromosScreen(
    currentOrderSubtotal: Double? = null,
    onSelectVoucher: ((String) -> Unit)? = null,
    onNavigateToRide: ((String) -> Unit)? = null,
    onNavigateToFood: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: PromosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var chooserVoucher by remember { mutableStateOf<Voucher?>(null) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            Toast.makeText(context, msg.asString(context), Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    fun handleUseVoucher(voucher: Voucher) {
        if (currentOrderSubtotal != null && onSelectVoucher != null) {
            // Checkout context: Apply voucher to current active order
            viewModel.onUseVoucherCard(voucher, currentOrderSubtotal, onSelectVoucher)
        } else {
            // Exploration context: Navigate directly to the corresponding service
            when (voucher.applicableService.uppercase()) {
                "RIDE" -> {
                    onNavigateToRide?.invoke(voucher.code)
                }
                "FOOD" -> {
                    onNavigateToFood?.invoke(voucher.code)
                }
                else -> {
                    // ALL service: open service chooser dialog
                    chooserVoucher = voucher
                }
            }
        }
    }

    OmniGoMainLayout(
        modifier = modifier,
        headerHeight = Dimen.HeightTopBar,
        header = {
            PromosTopBar(
                totalVouchersCount = uiState.allVouchersCount,
                onOpenHistory = viewModel::openUsageHistory
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimen.PaddingM),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.M),
            contentPadding = PaddingValues(
                top = AppSpacing.S,
                bottom = Dimen.HeightBottomNavPadding
            )
        ) {
            // 1. Hero Promo Banner
            item {
                PromosHeroBanner(totalCount = uiState.allVouchersCount)
            }

            // 2. Interactive Input Bar
            item {
                PromosInputBar(
                    code = uiState.promoCodeInput,
                    isLoading = uiState.isLoading,
                    onCodeChange = viewModel::updatePromoCodeInput,
                    onPasteCode = {
                        clipboardManager.getText()?.text?.let { text ->
                            if (text.isNotBlank()) {
                                viewModel.updatePromoCodeInput(text.trim())
                            }
                        }
                    },
                    onApply = {
                        viewModel.applyManualCode(currentOrderSubtotal, onSelectVoucher)
                    }
                )
            }

            // 3. Category Filter Pills
            item {
                PromosFilterPills(
                    selectedIndex = uiState.selectedFilterIndex,
                    allCount = uiState.allVouchersCount,
                    rideCount = uiState.rideVouchersCount,
                    foodCount = uiState.foodVouchersCount,
                    onSelectIndex = viewModel::selectFilter
                )
            }

            // 4. Voucher Tickets List / Loading / Empty
            if (uiState.isLoading) {
                item {
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
                }
            } else if (uiState.vouchers.isEmpty()) {
                item {
                    PromosEmptyState(
                        onResetFilter = { viewModel.selectFilter(0) }
                    )
                }
            } else {
                items(uiState.vouchers, key = { "${it.id}_${it.code}" }) { voucher ->
                    val isSelected = uiState.appliedPreview?.voucherCode.equals(voucher.code, ignoreCase = true)
                    VoucherItemCard(
                        voucher = voucher,
                        currentOrderSubtotal = currentOrderSubtotal,
                        isSelected = isSelected,
                        onUseClick = { selected ->
                            handleUseVoucher(selected)
                        },
                        onCopyCode = { code ->
                            clipboardManager.setText(AnnotatedString(code))
                            viewModel.copyCode(code)
                        },
                        onDetailClick = { selected ->
                            viewModel.openDetail(selected)
                        }
                    )
                }
            }
        }
    }

    // Modal Dialog: Voucher Detail
    uiState.detailVoucher?.let { voucher ->
        VoucherDetailDialog(
            voucher = voucher,
            onDismiss = viewModel::closeDetail,
            onUseClick = { selected ->
                viewModel.closeDetail()
                handleUseVoucher(selected)
            },
            onCopyCode = { code ->
                clipboardManager.setText(AnnotatedString(code))
                viewModel.copyCode(code)
            }
        )
    }

    // Dialog: Service Chooser for ALL service vouchers
    chooserVoucher?.let { voucher ->
        VoucherServiceChooserDialog(
            voucher = voucher,
            onChooseRide = { code ->
                chooserVoucher = null
                onNavigateToRide?.invoke(code)
            },
            onChooseFood = { code ->
                chooserVoucher = null
                onNavigateToFood?.invoke(code)
            },
            onDismiss = { chooserVoucher = null }
        )
    }

    // BottomSheet: Voucher Usage History
    if (uiState.showHistorySheet) {
        VoucherUsageHistorySheet(
            usages = uiState.usageHistory,
            isLoading = uiState.isLoadingHistory,
            onDismiss = viewModel::closeUsageHistory
        )
    }
}
