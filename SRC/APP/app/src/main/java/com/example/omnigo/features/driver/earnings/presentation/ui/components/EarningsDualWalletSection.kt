package com.example.omnigo.features.driver.earnings.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun EarningsDualWalletSection(
    incomeBalance: Double,
    depositBalance: Double,
    onCashoutClick: () -> Unit,
    onTopupClick: () -> Unit,
    onIncomeHistoryClick: () -> Unit,
    onDepositHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.M)
    ) {
        // Income Wallet (Ví thu nhập)
        WalletItemCard(
            title = stringResource(id = R.string.driver_wallet_income),
            balance = String.format(Locale.getDefault(), "%,.0f ₫", incomeBalance),
            actionText = stringResource(id = R.string.driver_cashout),
            icon = Icons.Default.AccountBalanceWallet,
            accentColor = PrimaryColor,
            badgeBgColor = PrimaryContainer,
            buttonBgColor = PrimaryColor,
            buttonTextColor = OnPrimaryColor,
            onActionClick = onCashoutClick,
            onHistoryClick = onIncomeHistoryClick,
            modifier = Modifier.weight(1f)
        )

        // Deposit Wallet (Ví ký quỹ)
        WalletItemCard(
            title = stringResource(id = R.string.driver_wallet_credit),
            balance = String.format(Locale.getDefault(), "%,.0f ₫", depositBalance),
            actionText = stringResource(id = R.string.driver_topup),
            icon = Icons.Default.MonetizationOn,
            accentColor = SecondaryColor,
            badgeBgColor = SecondaryContainer,
            buttonBgColor = SecondaryContainer,
            buttonTextColor = SecondaryColor,
            onActionClick = onTopupClick,
            onHistoryClick = onDepositHistoryClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WalletItemCard(
    title: String,
    balance: String,
    actionText: String,
    icon: ImageVector,
    accentColor: Color,
    badgeBgColor: Color,
    buttonBgColor: Color,
    buttonTextColor: Color,
    onActionClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXS),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingSM)
        ) {
            // Top Row: Badge Icon + (Title & Balance in 1 column) + Chevron > (Interactive)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onHistoryClick
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeWalletBadge)
                        .clip(CircleShape)
                        .background(badgeBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }

                Spacer(modifier = Modifier.padding(start = Dimen.PaddingS))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = balance,
                        style = MaterialTheme.typography.s16.bold(),
                        color = accentColor,
                        maxLines = 1
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(Dimen.SizeS)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Slim Pill Action Button
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(AppShape.PillShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBgColor,
                    contentColor = buttonTextColor
                ),
                contentPadding = PaddingValues(horizontal = Dimen.PaddingS),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightPillButtonSmall)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.s13.semiBold()
                )
            }
        }
    }
}
