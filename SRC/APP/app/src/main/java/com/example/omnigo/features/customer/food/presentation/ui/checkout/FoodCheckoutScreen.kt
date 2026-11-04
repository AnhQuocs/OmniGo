package com.example.omnigo.features.customer.food.presentation.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.presentation.viewmodel.FoodCheckoutViewModel
import com.example.omnigo.features.customer.wallet.presentation.ui.components.PaymentWebViewDialog
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodCheckoutScreen(
    onBackClick: () -> Unit,
    onOrderCreated: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodCheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.createdOrderId) {
        val orderId = uiState.createdOrderId
        if (orderId != null) {
            viewModel.onOrderCreatedHandled()
            onOrderCreated(orderId)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val err = uiState.errorMessage
        if (err != null) {
            snackbarHostState.showSnackbar(err.asString(context))
            viewModel.onClearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.checkout_title),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLight
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceLight,
                shadowElevation = AppSpacing.S
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.checkout_price_total),
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = String.format(Locale.getDefault(), "%,.0f đ", uiState.finalTotalAmount),
                            style = MaterialTheme.typography.s18.bold(),
                            color = PrimaryColor
                        )
                        if (uiState.discountAmount > 0) {
                            Text(
                                text = "-${String.format(Locale.getDefault(), "%,.0f đ", uiState.discountAmount)}",
                                style = MaterialTheme.typography.s13.normal(),
                                color = PrimaryColor
                            )
                        }
                    }

                    Button(
                        onClick = viewModel::onPlaceOrder,
                        enabled = !uiState.isPlacingOrder && uiState.items.isNotEmpty(),
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor,
                            disabledContainerColor = PrimaryColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.height(Dimen.HeightDefault)
                    ) {
                        if (uiState.isPlacingOrder) {
                            CircularProgressIndicator(
                                color = TextWhite,
                                modifier = Modifier.size(Dimen.SizeM),
                                strokeWidth = AppSpacing.XXS
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.checkout_btn_place_order),
                                style = MaterialTheme.typography.s16.bold(),
                                color = TextWhite
                            )
                        }
                    }
                }
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(Dimen.PaddingL),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.MediumLarge)
        ) {
            // 1. Delivery Address Card
            item {
                CheckoutDeliveryAddressSection(
                    address = uiState.deliveryAddress
                )
            }

            // 2. Order Summary Card
            item {
                CheckoutOrderSummarySection(
                    restaurantName = uiState.restaurantName,
                    items = uiState.items,
                    itemsSubtotal = uiState.itemsSubtotal,
                    deliveryFee = uiState.deliveryFee,
                    discountAmount = uiState.discountAmount,
                    totalAmount = uiState.finalTotalAmount
                )
            }

            // 2.5 Voucher Selector Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
                    onClick = { viewModel.openVoucherSheet() },
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppSpacing.XXS)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimen.PaddingM),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.checkout_voucher_title),
                                style = MaterialTheme.typography.s15.bold(),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.XXS))
                            val applied = uiState.appliedVoucher
                            if (applied != null && applied.discountAmount > 0) {
                                Text(
                                    text = stringResource(
                                        id = R.string.voucher_applied_badge,
                                        applied.voucherCode,
                                        com.example.omnigo.features.customer.food.presentation.format.formatFoodPrice(applied.discountAmount)
                                    ),
                                    style = MaterialTheme.typography.s13.bold(),
                                    color = PrimaryColor
                                )
                            } else {
                                Text(
                                    text = stringResource(id = R.string.checkout_voucher_select_hint),
                                    style = MaterialTheme.typography.s13.normal(),
                                    color = TextSecondary
                                )
                            }
                        }
                        Text(
                            text = ">",
                            style = MaterialTheme.typography.s16.bold(),
                            color = PrimaryColor
                        )
                    }
                }
            }

            // 3. Driver Note Input Section
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppSpacing.XXS)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimen.PaddingM)
                    ) {
                        Text(
                            text = stringResource(id = R.string.checkout_driver_note_title),
                            style = MaterialTheme.typography.s15.bold(),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.S))

                        OutlinedTextField(
                            value = uiState.driverNote,
                            onValueChange = viewModel::onDriverNoteChanged,
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.checkout_driver_note_hint),
                                    style = MaterialTheme.typography.s13.normal(),
                                    color = TextSecondary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.ShapeS),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            maxLines = 3
                        )
                    }
                }
            }

            // 4. Payment Method Section
            item {
                CheckoutPaymentMethodSection(
                    selectedMethod = uiState.selectedPaymentMethod,
                    onMethodSelected = viewModel::onPaymentMethodSelected,
                    walletBalance = uiState.walletBalance,
                    totalAmount = uiState.finalTotalAmount,
                    onTopUpClick = { viewModel.openQuickTopUp(true) }
                )
            }
        }
    }

    if (uiState.showVoucherSheet) {
        com.example.omnigo.features.customer.promos.presentation.ui.components.VoucherSelectionBottomSheet(
            vouchers = uiState.applicableVouchers,
            orderAmount = uiState.totalAmount,
            appliedVoucherCode = uiState.appliedVoucher?.voucherCode,
            isLoading = uiState.isApplyingVoucher,
            onApplyCode = { code -> viewModel.applyVoucherCode(code) },
            onRemoveVoucher = { viewModel.removeVoucher() },
            onDismiss = { viewModel.dismissVoucherSheet() }
        )
    }

    // In-App Payment WebView for VNPay / MoMo
    val webViewUrl = uiState.paymentWebViewUrl
    if (!webViewUrl.isNullOrBlank()) {
        PaymentWebViewDialog(
            url = webViewUrl,
            onPaymentSuccess = { viewModel.onPaymentWebViewSuccess() },
            onPaymentFailed = { err -> viewModel.onPaymentWebViewFailed(err) },
            onDismiss = { viewModel.onPaymentWebViewDismiss() }
        )
    }

    // In-Checkout Quick Top-up BottomSheet
    if (uiState.showQuickTopUp) {
        val deficit = (uiState.finalTotalAmount - (uiState.walletBalance ?: 0.0)).coerceAtLeast(0.0)
        QuickTopUpBottomSheet(
            deficitAmount = deficit,
            onConfirmTopUp = { amount -> viewModel.onQuickTopUpConfirm(amount) },
            onDismiss = { viewModel.openQuickTopUp(false) },
            isLoading = uiState.isTopUpLoading
        )
    }

    if (uiState.showWalletPaymentConfirm) {
        WalletPaymentConfirmBottomSheet(
            amount = uiState.finalTotalAmount,
            currentBalance = uiState.walletBalance ?: 0.0,
            restaurantName = uiState.restaurantName,
            onConfirm = viewModel::onConfirmWalletPayment,
            onDismiss = viewModel::dismissWalletPaymentConfirm,
            isLoading = uiState.isPlacingOrder
        )
    }
}
