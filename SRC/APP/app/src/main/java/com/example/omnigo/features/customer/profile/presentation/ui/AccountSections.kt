package com.example.omnigo.features.customer.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
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
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun AccountHeaderSection(
    userName: String,
    userPhone: String,
    userRole: String,
    avatarUrl: String?,
    isUploadingAvatar: Boolean,
    onProfileClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onProfileClick),
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(Dimen.PaddingXXS, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with edit badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Dimen.SizeXXLPlus)
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(Dimen.PaddingXXS, PrimaryColor, CircleShape)
                            .clickable(onClick = onAvatarClick)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                            .clickable(onClick = onAvatarClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "O",
                            style = MaterialTheme.typography.s18.bold(),
                            color = PrimaryColor
                        )
                    }
                }

                if (isUploadingAvatar) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = TextWhite,
                            modifier = Modifier.size(Dimen.SizeM)
                        )
                    }
                }

                // Small Camera icon badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(Dimen.SizeM)
                        .clip(CircleShape)
                        .background(PrimaryColor)
                        .border(Dimen.PaddingXXS, SurfaceLight, CircleShape)
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Avatar",
                        tint = TextWhite,
                        modifier = Modifier.size(Dimen.SizeIconXS)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.s16.bold(),
                    color = TextPrimary
                )

                Text(
                    text = userPhone,
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeXXS),
                    color = PrimaryContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = Dimen.PaddingXSPlus, vertical = Dimen.PaddingXXS)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeIconXS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Text(
                            text = if (userRole.equals("DRIVER", ignoreCase = true)) {
                                stringResource(id = R.string.profile_role_driver)
                            } else {
                                stringResource(id = R.string.profile_customer_member_badge)
                            },
                            style = MaterialTheme.typography.s10.bold(),
                            color = PrimaryColor
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "View Profile",
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(Dimen.SizeSM)
            )
        }
    }
}

@Composable
fun AccountQuickDashboard(
    balance: String,
    onWalletClick: () -> Unit,
    onPromoClick: () -> Unit,
    onActivityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppShape.ShapeL),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(Dimen.PaddingXXS, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimen.PaddingSM, horizontal = Dimen.PaddingM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wallet section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onWalletClick)
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeXLPlus)
                        .clip(RoundedCornerShape(AppShape.ShapeM))
                        .background(SecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = SecondaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.S))

                Column {
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

            // Promo item
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .clickable(onClick = onPromoClick)
                    .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalOffer,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = stringResource(id = R.string.profile_quick_promo),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextPrimary
                )
            }

            // Activity item
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(AppShape.ShapeM))
                    .clickable(onClick = onActivityClick)
                    .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS)
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = stringResource(id = R.string.profile_quick_activity),
                    style = MaterialTheme.typography.s12.medium(),
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = SurfaceLight
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))
                Text(
                    text = title,
                    style = MaterialTheme.typography.s14.medium(),
                    color = TextPrimary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(Dimen.SizeIconXS)
            )
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.account_logout_confirm_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.account_logout_confirm_msg),
                style = MaterialTheme.typography.s14.normal(),
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                shape = RoundedCornerShape(AppShape.ShapeS)
            ) {
                Text(
                    text = stringResource(id = R.string.account_logout),
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextWhite
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    style = MaterialTheme.typography.s14.medium(),
                    color = TextSecondary
                )
            }
        },
        shape = RoundedCornerShape(AppShape.ShapeL),
        containerColor = SurfaceLight
    )
}
