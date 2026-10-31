package com.example.omnigo.features.driver.profile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s20

@Composable
fun DriverAccountTopBar(
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.driver_nav_account),
                style = MaterialTheme.typography.s20.bold(),
                color = TextPrimary
            )
            Text(
                text = stringResource(id = R.string.driver_account_subtitle),
                style = MaterialTheme.typography.s13.normal(),
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(AppSpacing.S))

        Box {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.size(Dimen.SizeXLPlus)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(id = R.string.notifications_title),
                    tint = TextPrimary,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            }
            // Red unread badge dot
            Box(
                modifier = Modifier
                    .size(Dimen.PaddingS)
                    .offset(x = Dimen.SizeM, y = Dimen.PaddingS)
                    .clip(CircleShape)
                    .background(ErrorColor)
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(Dimen.SizeXLPlus)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(id = R.string.settings_title),
                tint = TextPrimary,
                modifier = Modifier.size(Dimen.SizeM)
            )
        }
    }
}

