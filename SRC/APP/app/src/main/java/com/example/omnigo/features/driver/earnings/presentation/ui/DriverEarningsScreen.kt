package com.example.omnigo.features.driver.earnings.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.driver.common.ui.components.DriverRefreshTitleBar
import com.example.omnigo.features.driver.earnings.presentation.viewmodel.DriverEarningsViewModel
import com.example.omnigo.features.driver.earnings.presentation.ui.components.EarningsPeriodFilterChips
import com.example.omnigo.features.driver.trips.presentation.ui.components.DriverDailyReportCard
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold
import java.util.Locale

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
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingM)
    ) {
        DriverRefreshTitleBar(
            title = stringResource(id = R.string.driver_nav_earnings),
            onRefreshClick = viewModel::refresh,
            isRefreshEnabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        EarningsPeriodFilterChips(
            selectedPeriod = uiState.selectedPeriod,
            onPeriodSelected = viewModel::onPeriodSelected
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        DriverDailyReportCard(
            report = report,
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        DriverWalletCard(
            title = stringResource(id = R.string.driver_wallet_income),
            balance = String.format(Locale.getDefault(), "%,.0f ₫", netEarnings),
            actionText = stringResource(id = R.string.driver_cashout),
            isPrimary = true,
            onActionClick = { viewModel.setShowCashoutDialog(true) }
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        DriverWalletCard(
            title = stringResource(id = R.string.driver_wallet_credit),
            balance = String.format(Locale.getDefault(), "%,.0f ₫", cashCollected),
            actionText = stringResource(id = R.string.driver_topup),
            isPrimary = false,
            onActionClick = { viewModel.setShowTopupDialog(true) }
        )

        Spacer(modifier = Modifier.height(AppSpacing.L))

        DriverQuestsCard(
            completedTrips = completedTrips,
            targetTrips = DAILY_QUEST_TARGET_TRIPS
        )

        Spacer(modifier = Modifier.height(AppSpacing.XXLPlus))
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
private fun DriverWalletCard(
    title: String,
    balance: String,
    actionText: String,
    isPrimary: Boolean,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                Text(
                    text = balance,
                    style = MaterialTheme.typography.s20.bold(),
                    color = if (isPrimary) PrimaryColor else SecondaryColor
                )
            }

            if (isPrimary) {
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        contentColor = OnPrimaryColor
                    )
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.s13.semiBold()
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.s13.semiBold()
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverQuestsCard(
    completedTrips: Int,
    targetTrips: Int,
    modifier: Modifier = Modifier
) {
    val progress = (completedTrips.toFloat() / targetTrips.toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Text(
                text = stringResource(id = R.string.driver_quests),
                style = MaterialTheme.typography.s15.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Text(
                text = stringResource(id = R.string.driver_earnings_quest_10_trips),
                style = MaterialTheme.typography.s13.medium(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            LinearProgressIndicator(
                progress = { progress },
                color = SecondaryColor,
                trackColor = SecondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.PaddingS)
                    .clip(RoundedCornerShape(AppShape.PillShape))
            )

            Spacer(modifier = Modifier.height(AppSpacing.XSPlus))

            Text(
                text = stringResource(
                    id = R.string.driver_earnings_quest_progress_format,
                    completedTrips.coerceAtMost(targetTrips),
                    targetTrips
                ),
                style = MaterialTheme.typography.s12.normal(),
                color = TextSecondary
            )
        }
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
        shape = RoundedCornerShape(AppShape.ShapeXL),
        containerColor = SurfaceLight,
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = OnPrimaryColor
                )
            ) {
                Text(
                    text = stringResource(id = R.string.driver_btn_understood),
                    style = MaterialTheme.typography.s14.bold()
                )
            }
        }
    )
}
