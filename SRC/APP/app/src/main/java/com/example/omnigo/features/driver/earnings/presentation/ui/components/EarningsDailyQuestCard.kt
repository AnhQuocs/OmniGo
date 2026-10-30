package com.example.omnigo.features.driver.earnings.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun EarningsDailyQuestCard(
    completedTrips: Int,
    targetTrips: Int,
    onQuestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (completedTrips.toFloat() / targetTrips.coerceAtLeast(1)).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.PaddingXS),
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onQuestClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeWalletBadge)
                        .clip(CircleShape)
                        .background(SecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = SecondaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }

                Spacer(modifier = Modifier.padding(start = Dimen.PaddingS))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.driver_quests),
                        style = MaterialTheme.typography.s14.semiBold(),
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(id = R.string.driver_earnings_quest_10_trips),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.PillShape))
                        .background(SecondaryContainer)
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_earnings_quest_reward),
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = SecondaryColor
                    )
                }

                Spacer(modifier = Modifier.padding(start = Dimen.PaddingXS))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(Dimen.SizeS)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.PaddingXSPlus)
                    .clip(RoundedCornerShape(AppShape.PillShape)),
                color = SecondaryColor,
                trackColor = SecondaryContainer
            )

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            Text(
                text = stringResource(
                    id = R.string.driver_earnings_quest_progress_format,
                    completedTrips,
                    targetTrips
                ),
                style = MaterialTheme.typography.s12.normal(),
                color = TextSecondary
            )
        }
    }
}
