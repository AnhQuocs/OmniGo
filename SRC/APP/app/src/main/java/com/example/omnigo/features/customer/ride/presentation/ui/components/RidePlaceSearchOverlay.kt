package com.example.omnigo.features.customer.ride.presentation.ui.components

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.omnigo.R
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.features.customer.ride.presentation.state.LocationTarget
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.OutlineLight
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.SurfaceVariantLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun RidePlaceSearchOverlay(
    target: LocationTarget,
    query: String,
    searchResults: List<LocationPoint>,
    savedAddresses: List<SavedAddress>,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onSelectPlace: (LocationPoint) -> Unit,
    onPickOnMap: (LocationTarget) -> Unit,
    onSelectCurrentLocation: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingS, vertical = Dimen.PaddingXS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextPrimary
                    )
                }

                val titleRes = if (target == LocationTarget.PICKUP) {
                    R.string.ride_pickup_label
                } else {
                    R.string.ride_search_destination_hint
                }

                Text(
                    text = stringResource(id = titleRes),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary,
                    modifier = Modifier.padding(start = Dimen.PaddingXS)
                )
            }

            if (target == LocationTarget.DROPOFF) {
                Surface(
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXS)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessColor,
                            modifier = Modifier.size(Dimen.SizeSM)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = stringResource(id = R.string.ride_search_step_dropoff_guide),
                            style = MaterialTheme.typography.s12.medium(),
                            color = TextPrimary
                        )
                    }
                }
            }

            // Search Input Box
            Surface(
                shape = RoundedCornerShape(AppShape.ShapeM),
                color = SurfaceVariantLight,
                border = androidx.compose.foundation.BorderStroke(Dimen.PaddingXXS, OutlineLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXS)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (target == LocationTarget.PICKUP) PrimaryColor else SecondaryColor,
                        modifier = Modifier.size(Dimen.SizeSM)
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.S))

                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.ride_search_input_placeholder),
                                style = MaterialTheme.typography.s14.normal(),
                                color = TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            textStyle = MaterialTheme.typography.s14.normal().copy(color = TextPrimary),
                            singleLine = true,
                            cursorBrush = SolidColor(PrimaryColor),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }

                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.size(Dimen.SizeSM)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(id = R.string.ride_clear_search),
                                tint = TextSecondary,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }
                    }
                }
            }

            // Shortcut: Current GPS location
            PlaceShortcutItem(
                icon = Icons.Default.MyLocation,
                titleRes = R.string.ride_my_location,
                onClick = onSelectCurrentLocation
            )

            HorizontalDivider(
                color = OutlineVariantLight,
                thickness = Dimen.PaddingXXS
            )

            // Shortcut: Pick directly on map
            PlaceShortcutItem(
                icon = Icons.Default.Map,
                titleRes = R.string.ride_search_pick_on_map,
                onClick = { onPickOnMap(target) }
            )

            HorizontalDivider(
                color = OutlineVariantLight,
                thickness = Dimen.PaddingXXS
            )

            // Results / Suggestions Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeXLPlus),
                            strokeWidth = Dimen.PaddingXS
                        )
                    }
                } else if (query.isNotBlank() && searchResults.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(searchResults) { place ->
                            SearchResultItem(
                                place = place,
                                onClick = { onSelectPlace(place) }
                            )
                            HorizontalDivider(
                                color = OutlineVariantLight,
                                thickness = Dimen.PaddingXXS,
                                modifier = Modifier.padding(start = Dimen.PaddingUltra)
                            )
                        }
                    }
                } else if (query.isNotBlank() && searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimen.PaddingXL),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.ride_search_no_results),
                            style = MaterialTheme.typography.s14.normal(),
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Blank query: Show saved places
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (savedAddresses.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(id = R.string.ride_saved_places),
                                    style = MaterialTheme.typography.s12.medium(),
                                    color = TextTertiary,
                                    modifier = Modifier.padding(
                                        start = Dimen.PaddingM,
                                        top = Dimen.PaddingM,
                                        bottom = Dimen.PaddingS
                                    )
                                )
                            }

                            items(savedAddresses) { saved ->
                                val place = LocationPoint.fromSavedAddress(saved)
                                SavedAddressItem(
                                    saved = saved,
                                    onClick = { onSelectPlace(place) }
                                )
                                HorizontalDivider(
                                    color = OutlineVariantLight,
                                    thickness = Dimen.PaddingXXS,
                                    modifier = Modifier.padding(start = Dimen.PaddingUltra)
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
private fun PlaceShortcutItem(
    icon: ImageVector,
    titleRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimen.SizeXL)
                .clip(CircleShape)
                .background(PrimaryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeSM)
            )
        }

        Spacer(modifier = Modifier.width(AppSpacing.M))

        Text(
            text = stringResource(id = titleRes),
            style = MaterialTheme.typography.s14.semiBold(),
            color = PrimaryColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(Dimen.SizeS)
        )
    }
}
