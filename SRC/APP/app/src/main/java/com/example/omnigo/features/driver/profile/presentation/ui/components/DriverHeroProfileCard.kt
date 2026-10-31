package com.example.omnigo.features.driver.profile.presentation.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.CrownGold
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.RatingGold
import com.example.omnigo.ui.theme.ShieldBadgeColor
import com.example.omnigo.ui.theme.SkyBlueContainer
import com.example.omnigo.ui.theme.SkyBluePrimary
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.semiBold
import java.util.Locale

@Composable
fun DriverHeroProfileCard(
    userName: String,
    userPhone: String,
    avatarUrl: String?,
    rating: Double,
    totalTrips: Int,
    joinedMonths: Int,
    joinedDays: Long,
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
                width = Dimen.BorderHairline,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background image covering the entire card
            Image(
                painter = painterResource(id = R.drawable.driver_profile_background),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )

            // Content column layered directly on top of the background
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingM)
            ) {
                // Top: Avatar + Name + Phone + Role
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with Camera Badge
                    Box(
                        modifier = Modifier.size(Dimen.SizeXXLPlus)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Dimen.SizeXXLPlus)
                                .clip(CircleShape)
                                .background(SkyBlueContainer)
                                .clickable(onClick = onAvatarClick),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploadingAvatar) {
                                CircularProgressIndicator(
                                    color = SkyBluePrimary,
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
                                    color = SkyBluePrimary
                                )
                            }
                        }

                        // Camera badge
                        Box(
                            modifier = Modifier
                                .size(Dimen.SizeM)
                                .align(Alignment.BottomEnd)
                                .offset(x = Dimen.PaddingXXS, y = Dimen.PaddingXXS)
                                .clip(CircleShape)
                                .background(SurfaceLight)
                                .border(Dimen.BorderHairline, CardBorderColor, CircleShape)
                                .clickable(onClick = onAvatarClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(Dimen.SizeIconXS)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))

                    // Name, Verified Badge, Phone, Role Pill
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.s18.bold(),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XS))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeS2)
                            )
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.XXS))

                        Text(
                            text = userPhone,
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.XSPlus))

                        Surface(
                            shape = RoundedCornerShape(AppShape.PillShape),
                            color = SkyBlueContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = Dimen.PaddingS,
                                    vertical = Dimen.PaddingXXS
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.BusinessCenter,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(Dimen.SizeIconXS)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.XS))
                                Text(
                                    text = stringResource(id = R.string.driver_account_partner_badge),
                                    style = MaterialTheme.typography.s12.semiBold(),
                                    color = SkyBluePrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Bottom: 3 Stats Counters stacked directly on the cloud waves
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stat 1: Rating (ic_star) - Dot separator (4.9)
                    DriverStatItem(
                        iconRes = R.drawable.ic_star,
                        iconTint = RatingGold,
                        value = String.format(Locale.US, "%.1f", rating),
                        label = stringResource(id = R.string.driver_account_stat_rating_label),
                        modifier = Modifier.weight(1f)
                    )

                    // Stat 2: Trips (ic_shield_star)
                    DriverStatItem(
                        iconRes = R.drawable.ic_shield_star,
                        iconTint = ShieldBadgeColor,
                        value = totalTrips.toString(),
                        label = stringResource(id = R.string.driver_account_stat_trips_label),
                        modifier = Modifier.weight(1f)
                    )

                    // Stat 3: Joined (ic_crown)
                    val joinedText = if (joinedMonths > 0) {
                        stringResource(id = R.string.driver_account_stat_joined_val, joinedMonths)
                    } else {
                        stringResource(id = R.string.driver_account_stat_joined_days_val, joinedDays)
                    }
                    DriverStatItem(
                        iconRes = R.drawable.ic_crown,
                        iconTint = CrownGold,
                        value = joinedText,
                        label = stringResource(id = R.string.driver_account_stat_joined_label),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.XS))
            }
        }
    }
}

@Composable
private fun DriverStatItem(
    iconRes: Int,
    iconTint: androidx.compose.ui.graphics.Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(Dimen.SizeS2)
            )
            Spacer(modifier = Modifier.width(AppSpacing.XS))
            Text(
                text = value,
                style = MaterialTheme.typography.s15.bold(),
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.XXS))
        Text(
            text = label,
            style = MaterialTheme.typography.s12.normal(),
            color = TextSecondary
        )
    }
}
