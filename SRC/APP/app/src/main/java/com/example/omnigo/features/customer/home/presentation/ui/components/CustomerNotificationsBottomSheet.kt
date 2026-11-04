package com.example.omnigo.features.customer.home.presentation.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

private data class NotificationItemData(
    val id: String,
    val titleRes: Int,
    val descRes: Int,
    val timeAgo: String,
    val icon: ImageVector,
    val iconTint: Color,
    val containerTint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerNotificationsBottomSheet(
    onDismissRequest: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val notifications = listOf(
        NotificationItemData(
            id = "notif_1",
            titleRes = R.string.home_notification_promo_title,
            descRes = R.string.home_notification_promo_desc,
            timeAgo = "10m",
            icon = Icons.Filled.LocalOffer,
            iconTint = PrimaryColor,
            containerTint = PrimaryContainer
        ),
        NotificationItemData(
            id = "notif_2",
            titleRes = R.string.home_notification_ride_title,
            descRes = R.string.home_notification_ride_desc,
            timeAgo = "1h",
            icon = Icons.Filled.Shield,
            iconTint = SuccessColor,
            containerTint = SuccessColor.copy(alpha = 0.12f)
        ),
        NotificationItemData(
            id = "notif_3",
            titleRes = R.string.home_notification_wallet_title,
            descRes = R.string.home_notification_wallet_desc,
            timeAgo = "2h",
            icon = Icons.Filled.AccountBalanceWallet,
            iconTint = SecondaryColor,
            containerTint = SecondaryContainer
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXXL, topEnd = AppShape.ShapeXXL),
        containerColor = SurfaceLight,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM)
                .padding(bottom = Dimen.PaddingL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.home_notifications_title),
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onMarkAllAsRead) {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Text(
                            text = stringResource(id = R.string.home_notifications_mark_read),
                            style = MaterialTheme.typography.s12.bold(),
                            color = PrimaryColor
                        )
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.S),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItemData) {
    Surface(
        shape = RoundedCornerShape(AppShape.ShapeM),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingSM),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXL)
                    .clip(CircleShape)
                    .background(item.containerTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = item.titleRes),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )
                    Text(
                        text = item.timeAgo,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.XXS))

                Text(
                    text = stringResource(id = item.descRes),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
            }
        }
    }
}

