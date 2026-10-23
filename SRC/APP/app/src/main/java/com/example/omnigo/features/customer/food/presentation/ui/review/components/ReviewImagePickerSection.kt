package com.example.omnigo.features.customer.food.presentation.ui.review.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewImagePickerSection(
    imageUrls: List<String>,
    isUploading: Boolean,
    canAddMore: Boolean,
    isReadOnly: Boolean,
    onImageSelected: (android.content.Context, Uri) -> Unit,
    onRemoveImage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(context, it) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.food_review_photos_title, imageUrls.size, 5),
            style = MaterialTheme.typography.s13.medium(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.S)
        ) {
            // Display already uploaded images
            imageUrls.forEachIndexed { index, url ->
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeUltra)
                        .clip(RoundedCornerShape(AppShape.ShapeM))
                        .border(Dimen.PaddingXXS, CardBorderColor, RoundedCornerShape(AppShape.ShapeM))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Review Photo ${index + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (!isReadOnly) {
                        IconButton(
                            onClick = { onRemoveImage(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(Dimen.PaddingXXS)
                                .size(Dimen.SizeSM)
                                .background(TextPrimary.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = SurfaceLight,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }
                    }
                }
            }

            // Uploading indicator
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeUltra)
                        .clip(RoundedCornerShape(AppShape.ShapeM))
                        .background(CardBorderColor),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SizeM),
                        color = PrimaryColor,
                        strokeWidth = Dimen.PaddingXXS
                    )
                }
            }

            // Add photo button
            if (canAddMore && !isUploading && !isReadOnly) {
                Box(
                    modifier = Modifier
                        .size(Dimen.SizeUltra)
                        .clip(RoundedCornerShape(AppShape.ShapeM))
                        .border(Dimen.PaddingXXS, PrimaryColor.copy(alpha = 0.4f), RoundedCornerShape(AppShape.ShapeM))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeM)
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.XXS))
                        Text(
                            text = stringResource(id = R.string.food_review_add_photo),
                            style = MaterialTheme.typography.s12.normal(),
                            color = PrimaryColor
                        )
                    }
                }
            }
        }
    }
}
