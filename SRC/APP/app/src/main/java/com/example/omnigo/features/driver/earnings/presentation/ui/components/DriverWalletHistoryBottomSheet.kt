package com.example.omnigo.features.driver.earnings.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.earnings.domain.model.DriverWalletTransaction
import com.example.omnigo.features.driver.earnings.domain.util.EarningsTripItem
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s18

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverWalletHistoryBottomSheet(
    initialTab: Int = 0, // 0: Deposit Wallet, 1: Trip Income
    depositBalance: Double = 0.0,
    incomeBalance: Double = 0.0,
    transactions: List<DriverWalletTransaction> = emptyList(),
    tripItems: List<EarningsTripItem> = emptyList(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
                .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
        ) {
            // Header with Title and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeWalletBadge)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.driver_wallet_history_title),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(Dimen.SizeL)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Dual Tab Switcher Pill
            DualWalletTabSwitcher(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            if (selectedTab == 0) {
                // TAB 0: Ví ký quỹ
                DepositWalletTabContent(
                    depositBalance = depositBalance,
                    transactions = transactions,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // TAB 1: Ví thu nhập cuốc xe
                IncomeWalletTabContent(
                    incomeBalance = incomeBalance,
                    tripItems = tripItems,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DualWalletTabSwitcher(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.PillShape))
            .background(SecondaryContainer.copy(alpha = 0.5f))
            .padding(AppSpacing.XXS)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            WalletTabPillItem(
                title = stringResource(id = R.string.driver_wallet_tab_deposit),
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            WalletTabPillItem(
                title = stringResource(id = R.string.driver_wallet_tab_income),
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WalletTabPillItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) SurfaceLight else androidx.compose.ui.graphics.Color.Transparent
    val textCol = if (isSelected) PrimaryColor else TextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppShape.PillShape))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = Dimen.PaddingS),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = if (isSelected) MaterialTheme.typography.s13.bold() else MaterialTheme.typography.s13.normal(),
            color = textCol
        )
    }
}
