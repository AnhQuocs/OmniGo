package com.example.omnigo.features.auth.presentation.ui.register.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.InputBackgroundColor
import com.example.omnigo.ui.theme.InputFocusedBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun DocumentUploadCard(
    title: String,
    localUri: Uri?,
    imageUrl: String?,
    isUploading: Boolean,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasImage = localUri != null || !imageUrl.isNullOrBlank()

    OutlinedCard(
        shape = RoundedCornerShape(AppShape.ShapeM),
        border = BorderStroke(
            width = Dimen.PaddingXXS,
            color = when {
                isUploading -> InputFocusedBorderColor
                hasImage -> SuccessColor.copy(alpha = 0.5f)
                else -> CardBorderColor
            }
        ),
        colors = CardDefaults.outlinedCardColors(containerColor = InputBackgroundColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            // Header: Title & Uploaded status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.s14.semiBold(),
                    color = TextPrimary
                )

                if (hasImage && !isUploading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Text(
                            text = stringResource(id = R.string.driver_doc_uploaded),
                            style = MaterialTheme.typography.s12.normal(),
                            color = SuccessColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.S))

            // Body: Upload state / Preview / Picker
            if (isUploading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.SizeUltra)
                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(AppShape.ShapeS))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            strokeWidth = Dimen.PaddingXXS,
                            modifier = Modifier.size(Dimen.SizeSM),
                            color = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.M))
                        Text(
                            text = stringResource(id = R.string.driver_doc_uploading),
                            style = MaterialTheme.typography.s13.normal(),
                            color = TextSecondary
                        )
                    }
                }
            } else if (hasImage) {
                // Image preview with change/delete actions
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimen.SizeUltra + Dimen.SizeMega)
                            .clip(RoundedCornerShape(AppShape.ShapeS))
                    ) {
                        AsyncImage(
                            model = localUri ?: imageUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Clear button overlay
                        IconButton(
                            onClick = onClearImage,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(Dimen.PaddingXS)
                                .size(Dimen.SizeSM)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(AppShape.ShapeXXS))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.XS))

                    // Reupload button
                    TextButton(
                        onClick = onPickImage,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.XXS))
                        Text(
                            text = stringResource(id = R.string.driver_doc_reupload),
                            style = MaterialTheme.typography.s12.semiBold(),
                            color = PrimaryColor
                        )
                    }
                }
            } else {
                // Empty state: Tap to upload
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.SizeUltra)
                        .background(Color.White, RoundedCornerShape(AppShape.ShapeS))
                        .clickable(onClick = onPickImage)
                        .padding(Dimen.PaddingM)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeL)
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = stringResource(id = R.string.driver_doc_upload_hint),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
