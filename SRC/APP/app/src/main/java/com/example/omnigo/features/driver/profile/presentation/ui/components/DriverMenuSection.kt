package com.example.omnigo.features.driver.profile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.MintGreenContainer
import com.example.omnigo.ui.theme.MintGreenPrimary
import com.example.omnigo.ui.theme.PeachOrangeContainer
import com.example.omnigo.ui.theme.PeachOrangePrimary
import com.example.omnigo.ui.theme.PurpleContainer
import com.example.omnigo.ui.theme.PurplePrimary
import com.example.omnigo.ui.theme.SkyBlueContainer
import com.example.omnigo.ui.theme.SkyBluePrimary
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun DriverMenuSection(
    onEditProfile: () -> Unit,
    onChangeLanguage: () -> Unit,
    onOpenTerms: () -> Unit,
    onHelpCenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Item 1: Hồ sơ cá nhân (Pastel Blue)
        DriverMenuItemCard(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            },
            iconBgColor = SkyBlueContainer,
            title = stringResource(id = R.string.driver_account_menu_profile_title),
            subtitle = stringResource(id = R.string.driver_account_menu_profile_sub),
            onClick = onEditProfile
        )

        Spacer(modifier = Modifier.height(AppSpacing.SPlus))

        // Item 2: Ngôn ngữ (Pastel Purple)
        DriverMenuItemCard(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            },
            iconBgColor = PurpleContainer,
            title = stringResource(id = R.string.driver_account_menu_lang_title),
            subtitle = stringResource(id = R.string.driver_account_menu_lang_sub),
            onClick = onChangeLanguage
        )

        Spacer(modifier = Modifier.height(AppSpacing.SPlus))

        // Item 3: Điều khoản & Chính sách bảo mật (Pastel Mint Green)
        DriverMenuItemCard(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shield_check),
                    contentDescription = null,
                    tint = MintGreenPrimary,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            },
            iconBgColor = MintGreenContainer,
            title = stringResource(id = R.string.driver_account_menu_terms_title),
            subtitle = stringResource(id = R.string.driver_account_menu_terms_sub),
            onClick = onOpenTerms
        )

        Spacer(modifier = Modifier.height(AppSpacing.SPlus))

        // Item 4: Trung tâm trợ giúp (Pastel Peach Orange)
        DriverMenuItemCard(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = PeachOrangePrimary,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            },
            iconBgColor = PeachOrangeContainer,
            title = stringResource(id = R.string.driver_account_menu_support_title),
            subtitle = stringResource(id = R.string.driver_account_menu_support_sub),
            onClick = onHelpCenter
        )
    }
}

@Composable
private fun DriverMenuItemCard(
    icon: @Composable () -> Unit,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = Dimen.BorderHairline,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular icon badge
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXLPlus)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))

            // Title & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.S))

            // Navigation Arrow >
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(Dimen.SizeM)
            )
        }
    }
}
