package com.example.omnigo.features.driver.profile.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.example.omnigo.R
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

private val VEHICLE_TYPES = listOf("MOTORBIKE", "CAR_4", "CAR_7")

@Composable
fun DriverUpdateVehicleDialog(
    currentVehicleType: String,
    currentPlate: String,
    currentBrand: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (vehicleType: String, licensePlate: String, vehicleBrand: String) -> Unit
) {
    var selectedType by remember(currentVehicleType) {
        mutableStateOf(currentVehicleType.ifBlank { "MOTORBIKE" }.uppercase())
    }
    var plate by remember(currentPlate) { mutableStateOf(currentPlate) }
    var brand by remember(currentBrand) { mutableStateOf(currentBrand.ifBlank { "Honda" }) }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(AppShape.ShapeXL),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimen.ElevationL),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingL)
            ) {
                Text(
                    text = stringResource(id = R.string.driver_vehicle_dialog_title),
                    style = MaterialTheme.typography.s18.bold(),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                Text(
                    text = stringResource(id = R.string.driver_vehicle_type),
                    style = MaterialTheme.typography.s13.semiBold(),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(AppSpacing.XS))
                VehicleTypeSelectorRow(
                    selectedType = selectedType,
                    onSelectType = { selectedType = it }
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text(stringResource(id = R.string.driver_license_plate)) },
                    placeholder = { Text(stringResource(id = R.string.driver_vehicle_plate_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = CardBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AppSpacing.SPlus))

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text(stringResource(id = R.string.driver_vehicle_brand_label)) },
                    placeholder = { Text(stringResource(id = R.string.driver_vehicle_brand_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = CardBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.S)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimen.HeightDefault)
                    ) {
                        Text(
                            text = stringResource(id = R.string.account_logout_cancel),
                            style = MaterialTheme.typography.s14.semiBold(),
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = { onSave(selectedType, plate.trim(), brand.trim()) },
                        enabled = !isLoading && plate.isNotBlank() && brand.isNotBlank(),
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimen.HeightDefault)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = TextWhite,
                                modifier = Modifier.size(Dimen.SizeM)
                            )
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverResubmitBottomSheet(
    currentFullName: String,
    currentLicenseNumber: String,
    currentVehicleType: String,
    currentPlate: String,
    currentBrand: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        fullName: String,
        licenseNumber: String,
        vehicleType: String,
        licensePlate: String,
        vehicleBrand: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var fullName by remember(currentFullName) { mutableStateOf(currentFullName) }
    var licenseNumber by remember(currentLicenseNumber) { mutableStateOf(currentLicenseNumber) }
    var selectedType by remember(currentVehicleType) {
        mutableStateOf(currentVehicleType.ifBlank { "MOTORBIKE" }.uppercase())
    }
    var plate by remember(currentPlate) { mutableStateOf(currentPlate) }
    var brand by remember(currentBrand) { mutableStateOf(currentBrand.ifBlank { "Honda" }) }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.PaddingL)
                .padding(bottom = Dimen.PaddingXL)
        ) {
            Text(
                text = stringResource(id = R.string.driver_resubmit_sheet_title),
                style = MaterialTheme.typography.s18.bold(),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(AppSpacing.XXS))
            Text(
                text = stringResource(id = R.string.driver_resubmit_sheet_desc),
                style = MaterialTheme.typography.s13.normal(),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(AppSpacing.M))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(stringResource(id = R.string.profile_fullname_label)) },
                singleLine = true,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = CardBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            OutlinedTextField(
                value = licenseNumber,
                onValueChange = { licenseNumber = it },
                label = { Text(stringResource(id = R.string.driver_resubmit_license_label)) },
                singleLine = true,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = CardBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            Text(
                text = stringResource(id = R.string.driver_vehicle_type),
                style = MaterialTheme.typography.s13.semiBold(),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(AppSpacing.XS))
            VehicleTypeSelectorRow(
                selectedType = selectedType,
                onSelectType = { selectedType = it }
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            OutlinedTextField(
                value = plate,
                onValueChange = { plate = it },
                label = { Text(stringResource(id = R.string.driver_license_plate)) },
                placeholder = { Text(stringResource(id = R.string.driver_vehicle_plate_label)) },
                singleLine = true,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = CardBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text(stringResource(id = R.string.driver_vehicle_brand_label)) },
                placeholder = { Text(stringResource(id = R.string.driver_vehicle_brand_label)) },
                singleLine = true,
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = CardBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppSpacing.L))

            Button(
                onClick = {
                    onSubmit(
                        fullName.trim(),
                        licenseNumber.trim(),
                        selectedType,
                        plate.trim(),
                        brand.trim()
                    )
                },
                enabled = !isLoading &&
                    fullName.isNotBlank() &&
                    licenseNumber.isNotBlank() &&
                    plate.isNotBlank() &&
                    brand.isNotBlank(),
                shape = RoundedCornerShape(AppShape.ShapeM),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.HeightDefault)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = TextWhite,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.driver_resubmit_btn),
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleTypeSelectorRow(
    selectedType: String,
    onSelectType: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS)
    ) {
        VEHICLE_TYPES.forEach { type ->
            val isSelected = selectedType.equals(type, ignoreCase = true)
            Surface(
                shape = RoundedCornerShape(AppShape.ShapeM),
                color = if (isSelected) PrimaryContainer else SurfaceLight,
                border = BorderStroke(
                    width = Dimen.BorderThin,
                    color = if (isSelected) PrimaryColor else CardBorderColor
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectType(type) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = Dimen.PaddingS),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.s12.semiBold(),
                        color = if (isSelected) PrimaryColor else TextPrimary
                    )
                }
            }
        }
    }
}
