package com.example.omnigo.features.customer.profile.presentation.ui

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.omnigo.R
import com.example.omnigo.features.customer.profile.presentation.ui.components.AvatarPickerBottomSheet
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountUiEvent
import com.example.omnigo.features.customer.profile.presentation.viewmodel.AccountViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.s20
import com.example.omnigo.utils.s24

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalProfileScreen(
    onBackClick: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFullAvatarDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.uploadAvatar(context, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.uploadAvatarBitmap(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.syncWithRemoteProfile()
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AccountUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AccountUiEvent.ShowStringResToast -> Toast.makeText(context, context.getString(event.resId), Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.profile_personal_info_title),
                        style = MaterialTheme.typography.s18.bold()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimen.PaddingL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(AppSpacing.L))

            // Avatar Section
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Dimen.SizeUltra)
            ) {
                if (!uiState.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uiState.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(Dimen.PaddingXXS, PrimaryColor, CircleShape)
                            .clickable { viewModel.onAvatarClick() }
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(PrimaryColor.copy(alpha = 0.15f))
                            .border(Dimen.PaddingXXS, PrimaryColor, CircleShape)
                            .clickable { viewModel.onAvatarClick() }
                    ) {
                        Text(
                            text = uiState.userName.firstOrNull()?.uppercase() ?: "O",
                            style = MaterialTheme.typography.s24.bold(),
                            color = PrimaryColor
                        )
                    }
                }

                if (uiState.isUploadingAvatar) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = TextWhite,
                            modifier = Modifier.size(Dimen.SizeL)
                        )
                    }
                }

                // Camera Badge Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(Dimen.SizeXL)
                        .clip(CircleShape)
                        .background(PrimaryColor)
                        .border(Dimen.PaddingXXS, MaterialTheme.colorScheme.background, CircleShape)
                        .clickable { viewModel.onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Avatar",
                        tint = TextWhite,
                        modifier = Modifier.size(Dimen.SizeS2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Text(
                text = uiState.userName,
                style = MaterialTheme.typography.s20.bold(),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(AppSpacing.XS))

            OutlinedButton(
                onClick = { viewModel.onAvatarClick() },
                shape = RoundedCornerShape(AppShape.ShapeL),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
            ) {
                Text(
                    text = stringResource(id = R.string.profile_avatar_edit_btn),
                    style = MaterialTheme.typography.s14.medium()
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.XL))

            // Details Group Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppShape.ShapeL),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = Dimen.PaddingXXS,
                border = androidx.compose.foundation.BorderStroke(Dimen.PaddingXXS, CardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(Dimen.PaddingML)
                ) {
                    ProfileFieldItem(
                        icon = Icons.Outlined.Person,
                        label = stringResource(id = R.string.profile_fullname_label),
                        value = uiState.userName
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.L))

                    ProfileFieldItem(
                        icon = Icons.Outlined.Phone,
                        label = stringResource(id = R.string.profile_phone_label),
                        value = uiState.userPhone,
                        badge = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AppShape.ShapeS))
                                    .background(PrimaryColor.copy(alpha = 0.1f))
                                    .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXXS)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(Dimen.SizeIconXS)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.XXS))
                                Text(
                                    text = stringResource(id = R.string.profile_verified_badge),
                                    style = MaterialTheme.typography.s12.medium(),
                                    color = PrimaryColor
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.L))

                    ProfileFieldItem(
                        icon = Icons.Outlined.Email,
                        label = stringResource(id = R.string.profile_email_label),
                        value = uiState.userEmail.ifBlank { "—" }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.L))

                    val roleLabel = if (uiState.userRole.equals("DRIVER", ignoreCase = true)) {
                        stringResource(id = R.string.profile_role_driver)
                    } else {
                        stringResource(id = R.string.profile_role_customer)
                    }

                    ProfileFieldItem(
                        icon = Icons.Outlined.Badge,
                        label = stringResource(id = R.string.profile_account_role),
                        value = roleLabel
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.XLPlus))
        }
    }

    if (uiState.showAvatarPicker) {
        AvatarPickerBottomSheet(
            hasCurrentAvatar = !uiState.avatarUrl.isNullOrBlank(),
            onDismiss = { viewModel.onDismissAvatarPicker() },
            onTakePhoto = {
                viewModel.onDismissAvatarPicker()
                cameraLauncher.launch(null)
            },
            onChooseGallery = {
                viewModel.onDismissAvatarPicker()
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onViewAvatar = {
                viewModel.onDismissAvatarPicker()
                showFullAvatarDialog = true
            }
        )
    }

    if (showFullAvatarDialog && !uiState.avatarUrl.isNullOrBlank()) {
        Dialog(onDismissRequest = { showFullAvatarDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppShape.ShapeL))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(Dimen.PaddingL),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uiState.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full Avatar",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(Dimen.SizeUltra * 2)
                )
            }
        }
    }
}

@Composable
private fun ProfileFieldItem(
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AppSpacing.XXS))
            Text(
                text = value,
                style = MaterialTheme.typography.s16.medium(),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        badge?.let {
            Spacer(modifier = Modifier.width(AppSpacing.S))
            it()
        }
    }
}

