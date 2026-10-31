package com.example.omnigo.features.driver.home.presentation.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.omnigo.R
import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.model.DriverDailyReport
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.MintGreenContainer
import com.example.omnigo.ui.theme.MintGreenPrimary
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold
import java.util.Locale

private val StatGoldColor = Color(0xFFFFA000)
private val StatGreenColor = Color(0xFF43A047)

@Composable
fun DriverHomeTopCard(
    profile: DriverAccountProfile?,
    approvalStatus: DriverApprovalStatus,
    dailyReport: DriverDailyReport?,
    rating: Double,
    totalTrips: Int,
    joinedMonths: Int,
    joinedDays: Long,
    modifier: Modifier = Modifier
) {
    val badgeBg = when (approvalStatus) {
        DriverApprovalStatus.APPROVED -> MintGreenContainer
        DriverApprovalStatus.PENDING_APPROVAL -> SecondaryContainer
        DriverApprovalStatus.REJECTED, DriverApprovalStatus.INACTIVE -> ErrorContainer
    }
    val badgeColor = when (approvalStatus) {
        DriverApprovalStatus.APPROVED -> MintGreenPrimary
        DriverApprovalStatus.PENDING_APPROVAL -> WarningColor
        DriverApprovalStatus.REJECTED, DriverApprovalStatus.INACTIVE -> ErrorColor
    }
    val badgeLabelRes = when (approvalStatus) {
        DriverApprovalStatus.APPROVED -> R.string.driver_approval_badge_approved
        DriverApprovalStatus.PENDING_APPROVAL -> R.string.driver_approval_badge_pending
        DriverApprovalStatus.REJECTED, DriverApprovalStatus.INACTIVE -> R.string.driver_approval_badge_rejected
    }

    val displayTripsCount = if (totalTrips > 0) totalTrips else (dailyReport?.totalTrips ?: 0)
    val joinedText = if (joinedMonths >= 1) {
        stringResource(id = R.string.driver_account_stat_joined_val, joinedMonths)
    } else {
        stringResource(id = R.string.driver_account_stat_joined_days_val, joinedDays)
    }

    // Pure elevated Box (NO Card/Surface tint, pure crisp SurfaceLight white)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = Dimen.PaddingS,
                shape = RoundedCornerShape(AppShape.ShapeXL),
                clip = false
            )
            .clip(RoundedCornerShape(AppShape.ShapeXL))
            .background(SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM)
        ) {
            // Row 1: Compact Profile & Today's Earnings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Driver Info with compact avatar (SizeXLPlus = 40dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeXLPlus)
                            .clip(CircleShape)
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profile?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile?.avatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(Dimen.SizeXLPlus)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeSM)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.XXS)
                        ) {
                            Text(
                                text = profile?.fullName?.ifBlank { null }
                                    ?: stringResource(id = R.string.ride_driver_name_fallback),
                                style = MaterialTheme.typography.s15.bold(),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.XXS))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppShape.ShapeS))
                                .background(badgeBg)
                                .padding(horizontal = Dimen.PaddingXSPlus, vertical = Dimen.PaddingXXS)
                        ) {
                            Text(
                                text = stringResource(id = badgeLabelRes),
                                style = MaterialTheme.typography.s10.semiBold(),
                                color = badgeColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.S))

                // Today Earnings
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(id = R.string.driver_today_earnings),
                        style = MaterialTheme.typography.s10.normal(),
                        color = TextSecondary
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f ₫", dailyReport?.driverEarnings ?: 0.0),
                        style = MaterialTheme.typography.s18.bold(),
                        color = PrimaryColor
                    )
                    Text(
                        text = stringResource(id = R.string.driver_cockpit_today_trips, dailyReport?.totalTrips ?: 0),
                        style = MaterialTheme.typography.s10.normal(),
                        color = TextSecondary
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimen.PaddingS),
                thickness = Dimen.PaddingXXS,
                color = CardBorderColor.copy(alpha = 0.5f)
            )

            // Row 2: 3 Compact Stats (Clean vertical stacked design)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stat 1: Rating
                HomeStatItem(
                    iconRes = R.drawable.ic_star,
                    iconTint = StatGoldColor,
                    value = String.format(Locale.US, "%.1f", rating),
                    label = stringResource(id = R.string.driver_account_stat_rating_label),
                    modifier = Modifier.weight(1f)
                )

                // Stat 2: Total Trips
                HomeStatItem(
                    iconRes = R.drawable.ic_shield_star,
                    iconTint = StatGreenColor,
                    value = displayTripsCount.toString(),
                    label = stringResource(id = R.string.driver_account_stat_trips_label),
                    modifier = Modifier.weight(1f)
                )

                // Stat 3: Joined tenure
                HomeStatItem(
                    iconRes = R.drawable.ic_crown,
                    iconTint = StatGoldColor,
                    value = joinedText,
                    label = stringResource(id = R.string.driver_account_stat_joined_label),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HomeStatItem(
    iconRes: Int,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(Dimen.SizeS)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.s12.bold(),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.XXS))
        Text(
            text = label,
            style = MaterialTheme.typography.s10.normal(),
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
