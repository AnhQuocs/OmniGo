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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.ErrorContainer
import com.example.omnigo.ui.theme.MintGreenContainer
import com.example.omnigo.ui.theme.MintGreenPrimary
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PeachOrangeContainer
import com.example.omnigo.ui.theme.PeachOrangePrimary
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.WarningColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.semiBold

@Composable
fun DriverVehicleInfoCard(
    vehiclePlate: String?,
    approvalStatus: DriverApprovalStatus,
    registrationDate: String,
    onPlateClick: () -> Unit = {},
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
                width = Dimen.BorderHairline,
                color = CardBorderColor,
                shape = RoundedCornerShape(AppShape.ShapeL)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            // Header: Car Icon + Title + Approval Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimen.SizeXL)
                            .clip(RoundedCornerShape(AppShape.ShapeS))
                            .background(PeachOrangeContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            tint = PeachOrangePrimary,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSpacing.SPlus))
                    Text(
                        text = stringResource(id = R.string.driver_vehicle_info),
                        style = MaterialTheme.typography.s15.bold(),
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(AppShape.PillShape),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = Dimen.PaddingS,
                            vertical = Dimen.PaddingXXS
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (approvalStatus == DriverApprovalStatus.APPROVED) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(Dimen.SizeIconXS)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        }
                        Text(
                            text = stringResource(id = badgeTextRes),
                            style = MaterialTheme.typography.s12.semiBold(),
                            color = badgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            // Row 1: Loại xe (OmniBike)
            VehicleDetailRow(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.TwoWheeler,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                },
                label = stringResource(id = R.string.driver_vehicle_type),
                value = {
                    Text(
                        text = stringResource(id = R.string.driver_account_vehicle_service_name),
                        style = MaterialTheme.typography.s13.semiBold(),
                        color = TextPrimary
                    )
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimen.PaddingS),
                color = OutlineVariantLight
            )

            // Row 2: Biển số xe (ic_rectangle_ellipsis)
            VehicleDetailRow(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rectangle_ellipsis),
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                },
                label = stringResource(id = R.string.driver_license_plate),
                value = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onPlateClick)
                    ) {
                        Text(
                            text = vehiclePlate?.ifBlank { null }
                                ?: stringResource(id = R.string.driver_account_plate_not_updated),
                            style = MaterialTheme.typography.s13.bold(),
                            color = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                    }
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimen.PaddingS),
                color = OutlineVariantLight
            )

            // Row 3: Ngày đăng ký (ic_notebook)
            VehicleDetailRow(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notebook),
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                },
                label = stringResource(id = R.string.driver_account_reg_date_label),
                value = {
                    Text(
                        text = registrationDate,
                        style = MaterialTheme.typography.s13.semiBold(),
                        color = TextPrimary
                    )
                }
            )
        }
    }
}

@Composable
private fun VehicleDetailRow(
    icon: @Composable () -> Unit,
    label: String,
    value: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(AppSpacing.SPlus))
            Text(
                text = label,
                style = MaterialTheme.typography.s13.normal(),
                color = TextSecondary
            )
        }
        value()
    }
}
