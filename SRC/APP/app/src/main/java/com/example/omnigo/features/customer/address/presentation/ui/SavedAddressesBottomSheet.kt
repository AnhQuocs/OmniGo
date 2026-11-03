package com.example.omnigo.features.customer.address.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.address.domain.model.SavedAddress
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedAddressesBottomSheet(
    addresses: List<SavedAddress>,
    isLoading: Boolean,
    onDismissRequest: () -> Unit,
    onSaveNewAddress: (label: String, address: String) -> Unit,
    onDeleteAddress: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onUpdateAddress: (id: Long, label: String, address: String) -> Unit = { _, _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isAddingNew by remember { mutableStateOf(false) }
    var editingAddressId by remember { mutableStateOf<Long?>(null) }
    var newLabel by remember { mutableStateOf("") }
    var newAddress by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingM)
                .padding(bottom = Dimen.PaddingXL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.saved_address_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary
                )

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.SizeUltra),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else if (isAddingNew || editingAddressId != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.SPlus)
                ) {
                    Text(
                        text = stringResource(
                            id = if (editingAddressId != null) R.string.saved_address_edit_title else R.string.saved_address_add
                        ),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )

                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        label = { Text(stringResource(id = R.string.saved_address_name_hint)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = CardBorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text(stringResource(id = R.string.saved_address_detail_hint)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = CardBorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                isAddingNew = false
                                editingAddressId = null
                                newLabel = ""
                                newAddress = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CardBorderColor),
                            shape = RoundedCornerShape(AppShape.ShapeM)
                        ) {
                            Text(
                                text = stringResource(id = R.string.account_logout_cancel),
                                style = MaterialTheme.typography.s13.normal(),
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(AppSpacing.SPlus))

                        Button(
                            onClick = {
                                if (newLabel.isNotBlank() && newAddress.isNotBlank()) {
                                    val targetId = editingAddressId
                                    if (targetId != null) {
                                        onUpdateAddress(targetId, newLabel, newAddress)
                                    } else {
                                        onSaveNewAddress(newLabel, newAddress)
                                    }
                                    isAddingNew = false
                                    editingAddressId = null
                                    newLabel = ""
                                    newAddress = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            shape = RoundedCornerShape(AppShape.ShapeM)
                        ) {
                            Text(
                                text = stringResource(id = R.string.saved_address_save_success),
                                style = MaterialTheme.typography.s13.bold(),
                                color = OnPrimaryColor
                            )
                        }
                    }
                }
            } else {
                if (addresses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimen.PaddingL),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.saved_address_empty),
                            style = MaterialTheme.typography.s14.normal(),
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.S),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(addresses, key = { it.id }) { addressItem ->
                            SavedAddressRow(
                                item = addressItem,
                                onEdit = {
                                    editingAddressId = addressItem.id
                                    newLabel = addressItem.label
                                    newAddress = addressItem.address
                                },
                                onDelete = { onDeleteAddress(addressItem.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.M))

                Button(
                    onClick = {
                        editingAddressId = null
                        newLabel = ""
                        newAddress = ""
                        isAddingNew = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(AppShape.ShapeL),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen.HeightDefault)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = OnPrimaryColor)
                    Spacer(modifier = Modifier.width(AppSpacing.XS))
                    Text(
                        text = stringResource(id = R.string.saved_address_add),
                        style = MaterialTheme.typography.s14.bold(),
                        color = OnPrimaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedAddressRow(
    item: SavedAddress,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when {
        item.label.contains("nhà", ignoreCase = true) || item.label.contains("home", ignoreCase = true) -> Icons.Default.Home
        item.label.contains("công ty", ignoreCase = true) || item.label.contains("work", ignoreCase = true) -> Icons.Default.Business
        else -> Icons.Default.LocationOn
    }

    Surface(
        shape = RoundedCornerShape(AppShape.ShapeM),
        color = SurfaceLight,
        modifier = modifier
            .fillMaxWidth()
            .border(Dimen.PaddingXXS, CardBorderColor, RoundedCornerShape(AppShape.ShapeM))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.PaddingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen.SizeM)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeS)
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.M))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )
                    if (item.isDefault) {
                        Spacer(modifier = Modifier.width(AppSpacing.XS))
                        Surface(
                            shape = RoundedCornerShape(AppShape.ShapeXXS),
                            color = PrimaryContainer
                        ) {
                            Text(
                                text = "Mặc định",
                                style = MaterialTheme.typography.s10.bold(),
                                color = PrimaryColor,
                                modifier = Modifier.padding(horizontal = Dimen.PaddingXS, vertical = Dimen.PaddingXXS)
                            )
                        }
                    }
                }

                Text(
                    text = item.address,
                    style = MaterialTheme.typography.s12.normal(),
                    color = TextSecondary
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = PrimaryColor
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = ErrorColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
