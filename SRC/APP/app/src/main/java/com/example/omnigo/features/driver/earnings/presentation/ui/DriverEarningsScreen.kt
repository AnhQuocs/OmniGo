package com.example.omnigo.features.driver.earnings.presentation.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.features.driver.earnings.presentation.ui.components.DriverCashoutBottomSheet
import com.example.omnigo.features.driver.earnings.presentation.ui.components.DriverQuestDetailDialog
import com.example.omnigo.features.driver.earnings.presentation.ui.components.DriverTopupBottomSheet
import com.example.omnigo.features.driver.earnings.presentation.ui.components.DriverWalletHistoryDialog
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsChartSection
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsDailyQuestCard
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsDualWalletSection
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsHeaderSection
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsHeroCard
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsPillTabSwitcher
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsTripDetailSection
import com.example.omnigo.features.driver.earnings.presentation.viewmodel.DriverEarningsViewModel
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight

private const val DAILY_QUEST_TARGET_TRIPS = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverEarningsScreen(
    modifier: Modifier = Modifier,
    onNavigateToTrips: () -> Unit = {},
    viewModel: DriverEarningsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val report = uiState.dailyReport
    val netEarnings = report?.driverEarnings ?: 0.0
    val cashCollected = report?.cashCollected ?: 0.0
    val walletBalance = uiState.wallet?.balance ?: netEarnings
    val depositBalance = uiState.wallet?.balance ?: cashCollected
    val completedTrips = report?.totalTrips ?: 0

    // Handle Toast notification
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    // Handle Open Payment Gateway URL (VNPAY / MoMo)
    LaunchedEffect(uiState.openPaymentUrl) {
        uiState.openPaymentUrl?.let { url ->
            runCatching {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            viewModel.clearOpenPaymentUrl()
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = viewModel::refresh,
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingS)
        ) {
            EarningsHeaderSection()

            Spacer(modifier = Modifier.height(AppSpacing.S))

            EarningsPillTabSwitcher(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = viewModel::onPeriodSelected
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            EarningsHeroCard(
                report = report,
                period = uiState.selectedPeriod
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            EarningsDualWalletSection(
                incomeBalance = netEarnings,
                depositBalance = depositBalance,
                onCashoutClick = { viewModel.setShowCashoutSheet(true) },
                onTopupClick = { viewModel.setShowTopupSheet(true) },
                onWalletHistoryClick = { viewModel.setShowWalletHistory(true) }
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            EarningsDailyQuestCard(
                completedTrips = completedTrips,
                targetTrips = DAILY_QUEST_TARGET_TRIPS,
                onQuestClick = { viewModel.setShowQuestDetail(true) }
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            EarningsChartSection(
                selectedPeriod = uiState.selectedPeriod,
                chartPoints = uiState.chartPoints,
                onPeriodSelected = viewModel::onPeriodSelected
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            EarningsTripDetailSection(
                tripItems = uiState.tripItems,
                onViewAllTripsClick = onNavigateToTrips
            )

            Spacer(modifier = Modifier.height(Dimen.HeightBottomNavPadding))
        }
    }

    // BottomSheet: Rút tiền về tài khoản ngân hàng
    if (uiState.showCashoutSheet) {
        DriverCashoutBottomSheet(
            availableBalance = walletBalance,
            isSubmitting = uiState.isActionLoading,
            onDismiss = { viewModel.setShowCashoutSheet(false) },
            onConfirmCashout = viewModel::confirmCashout
        )
    }

    // BottomSheet: Nạp tiền ví ký quỹ
    if (uiState.showTopupSheet) {
        DriverTopupBottomSheet(
            isSubmitting = uiState.isActionLoading,
            onDismiss = { viewModel.setShowTopupSheet(false) },
            onConfirmTopup = { amount, method -> viewModel.confirmTopup(amount, method) }
        )
    }

    // Dialog: Lịch sử biến động số dư ví
    if (uiState.showWalletHistory) {
        DriverWalletHistoryDialog(
            transactions = uiState.walletTransactions,
            onDismiss = { viewModel.setShowWalletHistory(false) }
        )
    }

    // Dialog: Chi tiết các mốc nhiệm vụ thưởng ngày
    if (uiState.showQuestDetail) {
        DriverQuestDetailDialog(
            completedTrips = completedTrips,
            onDismiss = { viewModel.setShowQuestDetail(false) }
        )
    }
}
