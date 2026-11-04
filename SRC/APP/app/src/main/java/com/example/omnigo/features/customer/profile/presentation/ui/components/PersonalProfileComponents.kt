package com.example.omnigo.features.customer.profile.presentation.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun ProfileFieldItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimen.SizeXLPlus)
                .clip(CircleShape)
                .background(PrimaryColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeSM)
            )
        }

        Spacer(modifier = Modifier.width(AppSpacing.MediumLarge))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.s12.medium(),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(AppSpacing.XXS))
            Text(
                text = value,
                style = MaterialTheme.typography.s16.medium(),
                color = TextPrimary
            )
        }

        badge?.let {
            Spacer(modifier = Modifier.width(AppSpacing.S))
            it()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(
    currentName: String,
    currentEmail: String,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (newName: String, newEmail: String?) -> Unit
) {
    var fullName by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL),
        containerColor = SurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingL)
                .padding(bottom = Dimen.PaddingXXL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.profile_edit_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.s12.medium(),
                    color = ErrorColor,
                    modifier = Modifier.padding(bottom = Dimen.PaddingS)
                )
            }

            Text(
                text = stringResource(id = R.string.profile_fullname_label),
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(AppSpacing.XS))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = { Text(stringResource(id = R.string.profile_edit_fullname_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.L))

            Text(
                text = stringResource(id = R.string.profile_email_label),
                style = MaterialTheme.typography.s14.semiBold(),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(AppSpacing.XS))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text(stringResource(id = R.string.profile_edit_email_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.XL))

            Button(
                onClick = { onSave(fullName, email.ifBlank { null }) },
                enabled = fullName.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault),
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(Dimen.SizeM))
                } else {
                    Text(
                        text = stringResource(id = R.string.profile_edit_save_btn),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextWhite
                    )
                }
            }
        }
    }
}

@Composable
fun FullAvatarDialog(
    avatarUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppShape.ShapeL))
                .background(SurfaceLight)
                .padding(Dimen.PaddingL),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Full Avatar",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(Dimen.SizeUltra * 2)
            )
        }
    }
}

@Composable
fun DeactivateAccountDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = stringResource(id = R.string.profile_deactivate_dialog_title),
                style = MaterialTheme.typography.s18.bold(),
                color = ErrorColor
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.profile_deactivate_dialog_desc),
                style = MaterialTheme.typography.s14.normal(),
                color = TextSecondary
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = ErrorColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.profile_deactivate_confirm_btn),
                        style = MaterialTheme.typography.s14.bold(),
                        color = ErrorColor
                    )
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(
                    text = stringResource(id = R.string.account_logout_cancel),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary
                )
            }
        },
        shape = RoundedCornerShape(AppShape.ShapeL),
        containerColor = SurfaceLight
    )
}
