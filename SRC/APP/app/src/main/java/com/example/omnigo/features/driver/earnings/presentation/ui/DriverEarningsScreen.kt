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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineLight
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

@Composable
fun DriverEarningsScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimen.PaddingL, vertical = Dimen.PaddingM)
    ) {
        Text(
            text = stringResource(id = R.string.driver_nav_earnings),
            style = MaterialTheme.typography.s18.bold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.MediumLarge))

        // Income Wallet Card
        DriverWalletCard(
            title = stringResource(id = R.string.driver_wallet_income),
            balance = "1.250.000 đ",
            actionText = stringResource(id = R.string.driver_cashout),
            isPrimary = true,
            onActionClick = {}
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Credit Wallet Card
        DriverWalletCard(
            title = stringResource(id = R.string.driver_wallet_credit),
            balance = "300.000 đ",
            actionText = stringResource(id = R.string.driver_topup),
            isPrimary = false,
            onActionClick = {}
        )

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // Quests / Bonus Incentive Section
        DriverQuestsCard()

        Spacer(modifier = Modifier.height(AppSpacing.XXL))
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
            Column {
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
                text = "Hoàn thành 10 cuốc xe hôm nay (+50.000 đ)",
                style = MaterialTheme.typography.s13.medium(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            LinearProgressIndicator(
                progress = { 0.8f },
                color = SecondaryColor,
                trackColor = SecondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.PaddingS)
                    .clip(RoundedCornerShape(AppShape.PillShape))
            )

            Spacer(modifier = Modifier.height(AppSpacing.XSPlus))

            Text(
                text = "Tiến độ: 8/10 cuốc",
                style = MaterialTheme.typography.s12.normal(),
                color = TextSecondary
            )
        }
    }
}
