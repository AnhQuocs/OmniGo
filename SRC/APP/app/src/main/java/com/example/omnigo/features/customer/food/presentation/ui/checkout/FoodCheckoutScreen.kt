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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = SurfaceLight,
                shadowElevation = Dimen.PaddingSM
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.checkout_price_total),
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "%,.0f ₫", uiState.totalAmount),
                            style = MaterialTheme.typography.s18.bold(),
                            color = PrimaryColor
                        )
                    }

                    Button(
                        onClick = viewModel::onPlaceOrder,
                        enabled = !uiState.isPlacingOrder && uiState.items.isNotEmpty(),
                        shape = RoundedCornerShape(AppShape.ShapeXL),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        modifier = Modifier.height(Dimen.HeightDefault)
                    ) {
                        if (uiState.isPlacingOrder) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimen.SizeM),
                                color = TextWhite,
                                strokeWidth = Dimen.PaddingXXS
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.checkout_btn_place_order),
                                style = MaterialTheme.typography.s14.bold(),
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
            contentPadding = PaddingValues(Dimen.PaddingM),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
        ) {
            // 1. Delivery Address Section
            item {
                CheckoutDeliveryAddressSection(address = uiState.deliveryAddress)
            }

            // 2. Order Summary Section
            item {
                CheckoutOrderSummarySection(
                    restaurantName = uiState.restaurantName,
                    items = uiState.items,
                    itemsSubtotal = uiState.itemsSubtotal,
                    deliveryFee = uiState.deliveryFee,
                    totalAmount = uiState.totalAmount
                )
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
                    onMethodSelected = viewModel::onPaymentMethodSelected
                )
            }
        }
    }
}
