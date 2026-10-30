package com.example.omnigo.features.driver.earnings.presentation.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineVariantLight
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
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun DriverQuestDetailDialog(
    completedTrips: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val milestones = listOf(
        QuestMilestone("Mốc 1 (5 cuốc)", "+20.000 ₫", completedTrips >= 5),
        QuestMilestone("Mốc 2 (10 cuốc)", "+50.000 ₫", completedTrips >= 10),
        QuestMilestone("Mốc 3 (15 cuốc)", "+100.000 ₫", completedTrips >= 15)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeWalletBadge)
                        .clip(CircleShape)
                        .background(SecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = SecondaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )
                }
                Text(
                    text = stringResource(id = R.string.driver_quest_modal_title),
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
            ) {
                milestones.forEachIndexed { index, m ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = m.title,
                                style = MaterialTheme.typography.s13.semiBold(),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.XXS))
                            Text(
                                text = if (m.isAchieved) "Đã hoàn thành" else "Chưa hoàn thành",
                                style = MaterialTheme.typography.s12.normal(),
                                color = if (m.isAchieved) SecondaryColor else TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppShape.PillShape))
                                .background(SecondaryContainer)
                                .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                        ) {
                            Text(
                                text = m.reward,
                                style = MaterialTheme.typography.s13.bold(),
                                color = SecondaryColor
                            )
                        }
                    }

                    if (index < milestones.size - 1) {
                        HorizontalDivider(
                            color = OutlineVariantLight.copy(alpha = 0.5f),
                            thickness = Dimen.PaddingXXS
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppShape.PillShape),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor)
            ) {
                Text(
                    text = stringResource(id = R.string.driver_btn_understood),
                    style = MaterialTheme.typography.s13.semiBold(),
                    color = OnPrimaryColor
                )
            }
        },
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(AppShape.ShapeL),
        modifier = modifier
    )
}

private data class QuestMilestone(
    val title: String,
    val reward: String,
    val isAchieved: Boolean
)

