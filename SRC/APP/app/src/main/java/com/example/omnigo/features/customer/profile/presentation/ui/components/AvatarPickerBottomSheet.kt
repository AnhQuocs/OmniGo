package com.example.omnigo.features.customer.profile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerBottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    hasCurrentAvatar: Boolean = false,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onViewAvatar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = AppShape.ShapeXL,
            topEnd = AppShape.ShapeXL
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingL)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(id = R.string.profile_avatar_picker_title),
                style = MaterialTheme.typography.s18.bold(),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Dimen.PaddingM)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = Dimen.PaddingXXS
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            PickerOptionItem(
                icon = Icons.Outlined.CameraAlt,
                label = stringResource(id = R.string.profile_avatar_take_photo),
                onClick = onTakePhoto
            )

            PickerOptionItem(
                icon = Icons.Outlined.PhotoLibrary,
                label = stringResource(id = R.string.profile_avatar_choose_gallery),
                onClick = onChooseGallery
            )

            if (hasCurrentAvatar) {
                PickerOptionItem(
                    icon = Icons.Outlined.AccountCircle,
                    label = stringResource(id = R.string.profile_avatar_view),
                    onClick = onViewAvatar
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.L))
        }
    }
}

@Composable
private fun PickerOptionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimen.PaddingML),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PrimaryColor,
            modifier = Modifier.size(Dimen.SizeL)
        )

        Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))

        Text(
            text = label,
            style = MaterialTheme.typography.s16.medium(),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
