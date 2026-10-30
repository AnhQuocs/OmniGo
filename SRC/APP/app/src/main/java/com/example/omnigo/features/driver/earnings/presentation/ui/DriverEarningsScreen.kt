package com.example.omnigo.features.driver.earnings.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsChartSection
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsDailyQuestCard
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsDualWalletSection
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsHeaderSection
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsHeroCard
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsPillTabSwitcher
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsTripDetailSection
import com.example.omnigo.features.driver.earnings.presentation.viewmodel.DriverEarningsViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18

private const val DAILY_QUEST_TARGET_TRIPS = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverEarningsScreen(
    modifier: Modifier = Modifier,
    viewModel: DriverEarningsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val report = uiState.dailyReport
    val netEarnings = report?.driverEarnings ?: 0.0
    val cashCollected = report?.cashCollected ?: 0.0
    val completedTrips = report?.totalTrips ?: 0

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
                depositBalance = cashCollected,
                onCashoutClick = { viewModel.setShowCashoutDialog(true) },
                onTopupClick = { viewModel.setShowTopupDialog(true) }
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            EarningsDailyQuestCard(
                completedTrips = completedTrips,
                targetTrips = DAILY_QUEST_TARGET_TRIPS
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            EarningsChartSection(
                selectedPeriod = uiState.selectedPeriod,
                chartPoints = uiState.chartPoints
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            EarningsTripDetailSection(
                tripItems = uiState.tripItems
            )

            Spacer(modifier = Modifier.height(Dimen.HeightBottomNavPadding))
        }
    }

    if (uiState.showCashoutDialog) {
        DriverWalletInfoDialog(
            title = stringResource(id = R.string.driver_wallet_dialog_cashout_title),
            message = stringResource(id = R.string.driver_wallet_dialog_cashout_msg),
            onDismiss = { viewModel.setShowCashoutDialog(false) }
        )
    }

    if (uiState.showTopupDialog) {
        DriverWalletInfoDialog(
            title = stringResource(id = R.string.driver_wallet_dialog_topup_title),
            message = stringResource(id = R.string.driver_wallet_dialog_topup_msg),
            onDismiss = { viewModel.setShowTopupDialog(false) }
        )
    }
}

@Composable
private fun DriverWalletInfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
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
            Text(
                text = message,
                style = MaterialTheme.typography.s14.normal(),
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(
                    text = stringResource(id = R.string.driver_btn_understood),
                    style = MaterialTheme.typography.s14.medium(),
                    color = OnPrimaryColor
                )
            }
        },
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(AppShape.ShapeL)
    )
}
