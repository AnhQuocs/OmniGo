package com.example.omnigo.features.customer.food.presentation.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.food.presentation.ui.review.components.DriverReviewTagsSection
import com.example.omnigo.features.customer.food.presentation.ui.review.components.RatingBarSection
import com.example.omnigo.features.customer.food.presentation.ui.review.components.ReviewImagePickerSection
import com.example.omnigo.features.customer.food.presentation.viewmodel.review.FoodReviewViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodReviewScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        val err = uiState.errorMessage
        if (err != null) {
            snackbarHostState.showSnackbar(err.asString(context))
            viewModel.onClearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        val msg = uiState.successMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg.asString(context))
            viewModel.onClearSuccessMessage()
            delay(1500)
            onBackClick()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.food_review_title),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.order_detail_action_back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryColor, strokeWidth = Dimen.PaddingXXS)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(Dimen.PaddingM),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.MediumLarge)
            ) {
                // Banner for Read-Only / Expired
                if (uiState.isExpired || uiState.isReadOnly) {
                    item {
                        ReviewStatusBanner(isExpired = uiState.isExpired)
                    }
                }

                // Section 1: Restaurant Rating & Comment
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                    ) {
                        Column(modifier = Modifier.padding(Dimen.PaddingM)) {
                            Text(
                                text = uiState.restaurantName ?: stringResource(id = R.string.food_review_restaurant_title),
                                style = MaterialTheme.typography.s16.bold(),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.XS))
                            Text(
                                text = stringResource(id = R.string.food_review_restaurant_subtitle),
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.M))
                            RatingBarSection(
                                rating = uiState.restaurantRating,
                                onRatingChanged = { viewModel.onRestaurantRatingChanged(it) },
                                isReadOnly = uiState.isReadOnly || uiState.isExpired
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.M))
                            OutlinedTextField(
                                value = uiState.restaurantComment,
                                onValueChange = { viewModel.onRestaurantCommentChanged(it) },
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.food_review_comment_placeholder),
                                        style = MaterialTheme.typography.s14.normal(),
                                        color = TextSecondary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                readOnly = uiState.isReadOnly || uiState.isExpired,
                                shape = RoundedCornerShape(AppShape.ShapeS),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = CardBorderColor
                                )
                            )
                        }
                    }
                }

                // Section 2: Review Images Picker
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                    ) {
                        Box(modifier = Modifier.padding(Dimen.PaddingM)) {
                            ReviewImagePickerSection(
                                imageUrls = uiState.uploadedImageUrls,
                                isUploading = uiState.isUploadingImage,
                                canAddMore = uiState.canAddMorePhotos,
                                isReadOnly = uiState.isReadOnly || uiState.isExpired,
                                onImageSelected = { ctx, uri -> viewModel.onUploadImage(ctx, uri) },
                                onRemoveImage = { viewModel.onRemoveImage(it) }
                            )
                        }
                    }
                }

                // Section 3: Driver Rating & Tags
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                    ) {
                        Column(modifier = Modifier.padding(Dimen.PaddingM)) {
                            Text(
                                text = uiState.driverName?.let {
                                    stringResource(id = R.string.food_review_driver_named_title, it)
                                } ?: stringResource(id = R.string.food_review_driver_title),
                                style = MaterialTheme.typography.s16.bold(),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.XS))
                            Text(
                                text = stringResource(id = R.string.food_review_driver_subtitle),
                                style = MaterialTheme.typography.s12.normal(),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.M))
                            RatingBarSection(
                                rating = uiState.driverRating,
                                onRatingChanged = { viewModel.onDriverRatingChanged(it) },
                                isReadOnly = uiState.isReadOnly || uiState.isExpired
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.M))
                            DriverReviewTagsSection(
                                availableTags = uiState.availableDriverTags,
                                selectedTags = uiState.selectedDriverTags,
                                onTagToggled = { viewModel.onToggleDriverTag(it) },
                                isReadOnly = uiState.isReadOnly || uiState.isExpired
                            )
                        }
                    }
                }

                // Section 4: Merchant Reply (if any)
                uiState.existingReview?.merchantReply?.let { reply ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.ShapeM),
                            colors = CardDefaults.cardColors(containerColor = PrimaryColor.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(Dimen.PaddingM)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = PrimaryColor,
                                        modifier = Modifier.size(Dimen.SizeSM)
                                    )
                                    Spacer(modifier = Modifier.padding(AppSpacing.XS))
                                    Text(
                                        text = stringResource(id = R.string.food_review_merchant_reply_title),
                                        style = MaterialTheme.typography.s14.bold(),
                                        color = PrimaryColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(AppSpacing.XS))
                                Text(
                                    text = reply,
                                    style = MaterialTheme.typography.s13.normal(),
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // Section 5: Submit Button
                if (!uiState.isReadOnly && !uiState.isExpired) {
                    item {
                        Button(
                            onClick = { viewModel.onSubmitReview() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimen.HeightDefault),
                            enabled = uiState.canSubmit,
                            shape = RoundedCornerShape(AppShape.ShapeM),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(Dimen.SizeM),
                                    color = SurfaceLight,
                                    strokeWidth = Dimen.PaddingXXS
                                )
                            } else {
                                Text(
                                    text = stringResource(id = R.string.food_review_submit_button),
                                    style = MaterialTheme.typography.s15.bold(),
                                    color = SurfaceLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewStatusBanner(isExpired: Boolean) {
    val bannerText = if (isExpired) {
        stringResource(id = R.string.food_review_banner_expired)
    } else {
        stringResource(id = R.string.food_review_banner_readonly)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TextSecondary.copy(alpha = 0.1f), RoundedCornerShape(AppShape.ShapeS))
            .padding(Dimen.PaddingM)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isExpired) Icons.Default.Info else Icons.Default.Lock,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(Dimen.SizeSM)
            )
            Spacer(modifier = Modifier.padding(AppSpacing.XS))
            Text(
                text = bannerText,
                style = MaterialTheme.typography.s12.medium(),
                color = TextSecondary
            )
        }
    }
}
