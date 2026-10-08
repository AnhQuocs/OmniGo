package com.example.omnigo.features.customer.food.presentation.ui.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.omnigo.features.customer.food.presentation.viewmodel.FoodOrderDetailViewModel
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
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s18

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodOrderDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodOrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        val err = uiState.errorMessage
        if (err != null && uiState.order != null) {
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
                        text = if (uiState.order != null) {
                            stringResource(id = R.string.order_detail_order_id, uiState.order?.id ?: 0L)
                        } else {
                            stringResource(id = R.string.order_detail_title)
                        },
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.order_detail_action_back),
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadOrderDetail(isRefresh = true) },
                        enabled = !uiState.isLoading && !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimen.SizeS),
                                color = PrimaryColor,
                                strokeWidth = Dimen.PaddingXXS
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(id = R.string.order_detail_action_refresh),
                                tint = PrimaryColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryColor,
                            strokeWidth = Dimen.PaddingXXS
                        )
                    }
                }
                uiState.order != null -> {
                    val order = uiState.order!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Dimen.PaddingM),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
                    ) {
                        // 1. Status Stepper / Timeline
                        item {
                            FoodOrderStatusTimeline(
                                status = order.status,
                                estimatedDeliveryMinutes = order.estimatedDeliveryMinutes
                            )
                        }

                        // 2. Driver Info Card
                        item {
                            val profile = uiState.driverProfile
                            DriverInfoCard(
                                driverId = order.driverId,
                                driverName = profile?.driverName
                                    ?.takeIf(String::isNotBlank) ?: order.driverName,
                                driverPhone = profile?.driverPhone
                                    ?.takeIf(String::isNotBlank) ?: order.driverPhone,
                                driverVehiclePlate = profile?.vehiclePlate
                                    ?.takeIf(String::isNotBlank) ?: order.driverVehiclePlate,
                                driverAvatarUrl = profile?.avatarUrl
                                    ?.takeIf(String::isNotBlank) ?: order.driverAvatarUrl,
                                status = order.status,
                                isProfileLoading = uiState.isDriverProfileLoading,
                                profileErrorMessage = uiState.driverProfileError?.asString(context)
                            )
                        }

                        // 3. Restaurant and Items
                        item {
                            OrderDetailRestaurantAndItemsSection(
                                restaurantName = order.restaurantName,
                                restaurantAddress = order.restaurantAddress,
                                items = order.items
                            )
                        }

                        // 4. Payment & Pricing Details
                        item {
                            OrderDetailPaymentAndPricingSection(
                                dropOffAddress = order.dropOffAddress,
                                note = order.note,
                                itemsPrice = order.itemsPrice,
                                deliveryFee = order.deliveryFee,
                                totalPrice = order.totalPrice,
                                paymentMethod = order.paymentMethod,
                                isPaid = order.isPaid
                            )
                        }
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimen.PaddingL),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage?.asString(context)
                                ?: stringResource(id = R.string.order_detail_error_title),
                            style = MaterialTheme.typography.s15.normal(),
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.M))

                        Button(
                            onClick = {
                                if (uiState.canRetry) viewModel.onRetry() else onBackClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text(
                                text = stringResource(
                                    id = if (uiState.canRetry) {
                                        R.string.order_detail_retry
                                    } else {
                                        R.string.order_detail_action_back
                                    }
                                ),
                                style = MaterialTheme.typography.s14.bold(),
                                color = TextWhite
                            )
                        }
                    }
                }
            }
        }
    }
}
