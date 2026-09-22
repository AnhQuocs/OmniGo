package com.example.omnigo.features.customer.home.presentation.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.omnigo.R
import com.example.omnigo.features.customer.home.domain.model.SavedAddress
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

@Composable
fun LocationPromptDialog(
    savedAddresses: List<SavedAddress>,
    onEnableGpsClick: () -> Unit,
    onAddNewAddressClick: () -> Unit,
    onSelectSavedAddress: (SavedAddress) -> Unit,
    onEditSavedAddress: (SavedAddress) -> Unit,
    onDeleteSavedAddress: (SavedAddress) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingML),
            shape = RoundedCornerShape(AppShape.ShapeXL),
            color = SurfaceLight,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.location_dialog_title),
                        style = MaterialTheme.typography.s16.bold(),
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(Dimen.SizeM)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // GPS Action Button
                Button(
                    onClick = {
                        onEnableGpsClick()
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault),
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = stringResource(id = R.string.location_btn_enable_gps),
                            style = MaterialTheme.typography.s14.bold(),
                            color = TextWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.SPlus))

                // Add New Address Button
                OutlinedButton(
                    onClick = {
                        onDismissRequest()
                        onAddNewAddressClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault),
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(Dimen.SizeS)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.S))
                        Text(
                            text = stringResource(id = R.string.location_add_new_address),
                            style = MaterialTheme.typography.s14.bold(),
                            color = PrimaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Saved Addresses Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.location_saved_addresses_title),
                        style = MaterialTheme.typography.s13.bold(),
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.S))

                if (savedAddresses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimen.PaddingM),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.location_no_saved_addresses),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(savedAddresses, key = { it.id }) { address ->
                            SavedAddressItem(
                                address = address,
                                onSelect = {
                                    onSelectSavedAddress(address)
                                    onDismissRequest()
                                },
                                onEdit = {
                                    onDismissRequest()
                                    onEditSavedAddress(address)
                                },
                                onDelete = { onDeleteSavedAddress(address) }
                            )
                            HorizontalDivider(color = CardBorderColor, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedAddressItem(
    address: SavedAddress,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = Dimen.PaddingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimen.SizeL)
                .clip(CircleShape)
                .background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeSM)
            )
        }

        Spacer(modifier = Modifier.width(AppSpacing.SPlus))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = address.addressName,
                style = MaterialTheme.typography.s13.bold(),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = address.fullAddress,
                style = MaterialTheme.typography.s12.normal(),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(Dimen.SizeL)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(id = R.string.location_dialog_edit_address),
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeS)
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(Dimen.SizeL)
        ) {
            Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = stringResource(id = R.string.location_dialog_delete_address),
                tint = ErrorColor,
                modifier = Modifier.size(Dimen.SizeS)
            )
        }
    }
}
