package com.example.omnigo.features.customer.profile.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14

@Composable
fun AccountQuickDashboard(
    balance: String,
    onWalletClick: () -> Unit,
    onPromoClick: () -> Unit,
    onActivityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
    ) {
        // OmniPay Card
        Card(
            modifier = Modifier
                .weight(1.3f)
                .clickable(onClick = onWalletClick),
            shape = RoundedCornerShape(AppShape.ShapeL),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
            border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
        ) {
            Column(
                modifier = Modifier.padding(Dimen.PaddingS),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeM)
                            .clip(CircleShape)
                            .background(PrimaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(Dimen.SizeIconXS)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeIconXS)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeIconXS)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                Text(
                    text = stringResource(id = R.string.profile_quick_wallet),
                    style = MaterialTheme.typography.s10.medium(),
                    color = TextSecondary
                )

                Text(
                    text = balance,
                    style = MaterialTheme.typography.s14.bold(),
                    color = TextPrimary
                )
            }
        }

        // Promo Card
        QuickActionElevatedCard(
            title = stringResource(id = R.string.profile_quick_promo),
            subtitle = stringResource(id = R.string.profile_metric_promos),
            icon = Icons.Outlined.LocalOffer,
            onClick = onPromoClick,
            modifier = Modifier.weight(1f)
        )

        // Activity Card
        QuickActionElevatedCard(
            title = stringResource(id = R.string.profile_quick_activity),
            subtitle = stringResource(id = R.string.activity_tab_history),
            icon = Icons.Outlined.History,
            onClick = onActivityClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionElevatedCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationS),
        border = BorderStroke(Dimen.BorderHairline, CardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(Dimen.PaddingS),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeM)
                    .clip(CircleShape)
                    .background(SecondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SecondaryColor,
                    modifier = Modifier.size(Dimen.SizeIconXS)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            Text(
                text = title,
                style = MaterialTheme.typography.s10.medium(),
                color = TextSecondary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.s12.bold(),
                color = TextPrimary
            )
        }
    }
}
