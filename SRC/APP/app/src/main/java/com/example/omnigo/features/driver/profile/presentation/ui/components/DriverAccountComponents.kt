package com.example.omnigo.features.driver.profile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold

@Composable
fun DriverProfileHeaderCard(
    userName: String,
    userPhone: String,
    avatarUrl: String?,
    approvalStatus: DriverApprovalStatus,
    isUploadingAvatar: Boolean,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeXXLPlus)
                    .clip(CircleShape)
                    .background(PrimaryContainer)
                    .clickable(onClick = onAvatarClick),
                contentAlignment = Alignment.Center
            ) {
                if (isUploadingAvatar) {
                    CircularProgressIndicator(
                        color = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM),
                        strokeWidth = Dimen.PaddingXXS
                    )
                } else if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = userName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(Dimen.SizeXXLPlus)
                    )
                } else {
                    Text(
                        text = userName.take(1).uppercase(),
                        style = MaterialTheme.typography.s20.bold(),
                        color = PrimaryColor
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
                Spacer(modifier = Modifier.height(AppSpacing.XXS))
                Text(
                    text = userPhone,
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeXS),
                    color = PrimaryContainer
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_role_driver),
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = PrimaryColor,
                        modifier = Modifier.padding(
                            horizontal = Dimen.PaddingS,
                            vertical = Dimen.PaddingXXS
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DriverVehicleInfoCard(
    vehiclePlate: String?,
    approvalStatus: DriverApprovalStatus,
    modifier: Modifier = Modifier
) {
    val badgeBg = when (approvalStatus) {
        DriverApprovalStatus.APPROVED -> SuccessColor.copy(alpha = 0.14f)
        DriverApprovalStatus.PENDING_APPROVAL -> SecondaryContainer
        DriverApprovalStatus.REJECTED, DriverApprovalStatus.INACTIVE -> ErrorContainer
    }
    val badgeColor = when (approvalStatus) {
        DriverApprovalStatus.APPROVED -> SuccessColor
        DriverApprovalStatus.PENDING_APPROVAL -> WarningColor
        DriverApprovalStatus.REJECTED, DriverApprovalStatus.INACTIVE -> ErrorColor
    }
    val badgeTextRes = when (approvalStatus) {
        DriverApprovalStatus.APPROVED -> R.string.driver_approval_badge_approved
        DriverApprovalStatus.PENDING_APPROVAL -> R.string.driver_approval_badge_pending
        DriverApprovalStatus.REJECTED, DriverApprovalStatus.INACTIVE -> R.string.driver_approval_badge_rejected
    }

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
                text = stringResource(id = R.string.driver_vehicle_info),
                style = MaterialTheme.typography.s15.bold(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.driver_account_status_label),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppShape.ShapeS))
                        .background(badgeBg)
                        .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                ) {
                    Text(
                        text = stringResource(id = badgeTextRes),
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = badgeColor
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimen.PaddingS),
                color = OutlineVariantLight
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.driver_vehicle_type),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Text(
                    text = stringResource(id = R.string.ride_vehicle_bike_title),
                    style = MaterialTheme.typography.s13.semiBold(),
                    color = TextPrimary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimen.PaddingS),
                color = OutlineVariantLight
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.driver_license_plate),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
                Text(
                    text = vehiclePlate?.ifBlank { null }
                        ?: stringResource(id = R.string.driver_account_plate_not_updated),
                    style = MaterialTheme.typography.s13.semiBold(),
                    color = PrimaryColor
                )
            }
        }
    }
}

@Composable
fun DriverSettingsCard(
    onEditProfile: () -> Unit,
    onChangeLanguage: () -> Unit,
    onOpenTerms: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(AppShape.ShapeL),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = Dimen.PaddingXXS,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            DriverSettingsRow(
                icon = Icons.Default.Person,
                title = stringResource(id = R.string.profile_personal_info_title),
                onClick = onEditProfile
            )
            HorizontalDivider(color = OutlineVariantLight)
            DriverSettingsRow(
                icon = Icons.Default.Language,
                title = stringResource(id = R.string.language),
                onClick = onChangeLanguage
            )
            HorizontalDivider(color = OutlineVariantLight)
            DriverSettingsRow(
                icon = Icons.Default.Policy,
                title = stringResource(id = R.string.account_terms),
                onClick = onOpenTerms
            )
        }
    }
}

@Composable
private fun DriverSettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = PrimaryColor,
            modifier = Modifier.size(Dimen.SizeM)
        )
        Spacer(modifier = Modifier.width(AppSpacing.M))
        Text(
            text = title,
            style = MaterialTheme.typography.s14.medium(),
            color = TextPrimary
        )
    }
}
