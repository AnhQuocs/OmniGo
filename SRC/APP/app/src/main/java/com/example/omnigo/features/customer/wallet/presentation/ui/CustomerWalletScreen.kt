package com.example.omnigo.features.customer.wallet.presentation.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.omnigo.features.customer.wallet.presentation.ui.components.WalletAmountDialog
import com.example.omnigo.features.customer.wallet.presentation.ui.components.WalletTransactionCard
import com.example.omnigo.features.customer.wallet.presentation.viewmodel.CustomerWalletViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s28
import com.example.omnigo.utils.semiBold
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CustomerWalletScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerWalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val vndFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    LaunchedEffect(uiState.openPaymentUrl) {
        uiState.openPaymentUrl?.let { url ->
            Toast.makeText(
                context,
                context.getString(R.string.wallet_deposit_redirect_msg),
                Toast.LENGTH_SHORT
            ).show()
            runCatching {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            viewModel.clearOpenPaymentUrl()
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            val display = if (msg == "OK") {
                context.getString(R.string.wallet_withdraw_success)
            } else {
                msg
            }
            Toast.makeText(context, display, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight)
                .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = TextPrimary
                )
            }
            Text(
                text = stringResource(id = R.string.wallet_screen_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = PrimaryColor
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimen.PaddingM),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.M)
        ) {
            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.s14.normal(),
                        color = ErrorColor
                    )
                }
            }

            // Hero Balance Card
            item {
                Card(
                    shape = RoundedCornerShape(AppShape.ShapeXL),
                    colors = CardDefaults.cardColors(containerColor = PrimaryColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationM),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimen.PaddingL)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.wallet_balance_label),
                                style = MaterialTheme.typography.s14.medium(),
                                color = OnPrimaryColor.copy(alpha = 0.85f)
                            )
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = OnPrimaryColor,
                                modifier = Modifier.size(Dimen.SizeL)
                            )
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.S))
                        Text(
                            text = "${vndFormat.format(uiState.balance)} ₫",
                            style = MaterialTheme.typography.s28.bold(),
                            color = OnPrimaryColor
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.L))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
                        ) {
                            Button(
                                onClick = { viewModel.setShowDepositDialog(true) },
                                shape = RoundedCornerShape(AppShape.ShapeM),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OnPrimaryColor,
                                    contentColor = PrimaryColor
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(Dimen.SizeS2)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.XS))
                                Text(
                                    text = stringResource(id = R.string.wallet_btn_deposit),
                                    style = MaterialTheme.typography.s14.bold(),
                                    color = PrimaryColor
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.setShowWithdrawDialog(true) },
                                shape = RoundedCornerShape(AppShape.ShapeM),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = OnPrimaryColor
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        Dimen.BorderHairline,
                                        OnPrimaryColor.copy(alpha = 0.6f),
                                        RoundedCornerShape(AppShape.ShapeM)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = OnPrimaryColor,
                                    modifier = Modifier.size(Dimen.SizeS2)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.XS))
                                Text(
                                    text = stringResource(id = R.string.wallet_btn_withdraw),
                                    style = MaterialTheme.typography.s14.bold(),
                                    color = OnPrimaryColor
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimen.PaddingXL),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }
            } else {
                items(uiState.transactions, key = { it.id }) { tx ->
                    WalletTransactionCard(tx = tx)
                }
            }
        }
    }

    if (uiState.showDepositDialog) {
        WalletAmountDialog(
            title = stringResource(id = R.string.wallet_deposit_dialog_title),
            showMethodSelector = true,
            isSubmitting = uiState.isActionLoading,
            onDismiss = { viewModel.setShowDepositDialog(false) },
            onConfirm = { amount, method -> viewModel.deposit(amount, method) }
        )
    }

    if (uiState.showWithdrawDialog) {
        WalletAmountDialog(
            title = stringResource(id = R.string.wallet_withdraw_dialog_title),
            showMethodSelector = false,
            isSubmitting = uiState.isActionLoading,
            onDismiss = { viewModel.setShowWithdrawDialog(false) },
            onConfirm = { amount, _ -> viewModel.withdraw(amount) }
        )
    }
}
