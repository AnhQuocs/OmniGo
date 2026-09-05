package com.example.omnigo.features.auth.presentation.ui.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.omnigo.R
import com.example.omnigo.core.components.AppTextField
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.InputBackgroundColor
import com.example.omnigo.ui.theme.InputFocusedBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.RoleCardSelectedBg
import com.example.omnigo.ui.theme.RoleCardSelectedBorder
import com.example.omnigo.ui.theme.RoleCardUnselectedBg
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun AccountInfoSection(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    isConfirmPasswordVisible: Boolean,
    onToggleConfirmPasswordVisibility: () -> Unit,
    isCustomer: Boolean,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    val isConfirmPasswordError = confirmPassword.isNotBlank() && confirmPassword != password

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.step_info),
            style = MaterialTheme.typography.s16.semiBold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        // Full Name
        AppTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = stringResource(id = R.string.full_name),
            placeholder = stringResource(id = R.string.full_name_hint),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Email
        AppTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(id = R.string.email),
            placeholder = if (isCustomer) stringResource(id = R.string.email_hint) else stringResource(id = R.string.email_required_hint),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Password
        AppTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(id = R.string.password),
            placeholder = stringResource(id = R.string.password_hint),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Confirm Password
        AppTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(id = R.string.confirm_password),
            placeholder = stringResource(id = R.string.confirm_password_hint),
            isError = isConfirmPasswordError,
            supportingText = if (isConfirmPasswordError) {
                {
                    Text(
                        text = stringResource(id = R.string.password_not_match),
                        color = ErrorColor,
                        style = MaterialTheme.typography.s12.normal()
                    )
                }
            } else null,
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                    Icon(
                        imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (isCustomer) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }
            )
        )
    }
}

@Composable
fun DriverInfoSection(
    selectedVehicleType: VehicleType,
    onVehicleTypeSelected: (VehicleType) -> Unit,
    isDropdownExpanded: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit,
    licensePlate: String,
    onLicensePlateChange: (String) -> Unit,
    vehicleModel: String,
    onVehicleModelChange: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.vehicle_type),
            style = MaterialTheme.typography.s16.semiBold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        // Modern Vehicle Type Selector Card
        ModernVehicleTypeSelector(
            selectedType = selectedVehicleType,
            isExpanded = isDropdownExpanded,
            onToggleExpand = { onDropdownExpandedChange(!isDropdownExpanded) },
            onSelectType = {
                onVehicleTypeSelected(it)
                onDropdownExpandedChange(false)
            }
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // License Plate
        AppTextField(
            value = licensePlate,
            onValueChange = { onLicensePlateChange(it.uppercase()) },
            label = stringResource(id = R.string.license_plate),
            placeholder = stringResource(id = R.string.license_plate_hint),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        // Vehicle Model
        AppTextField(
            value = vehicleModel,
            onValueChange = onVehicleModelChange,
            label = stringResource(id = R.string.vehicle_model),
            placeholder = stringResource(id = R.string.vehicle_model_hint),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
    }
}

@Composable
private fun ModernVehicleTypeSelector(
    selectedType: VehicleType,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelectType: (VehicleType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Selected Header Box
        OutlinedCard(
            onClick = onToggleExpand,
            shape = RoundedCornerShape(AppShape.ShapeM),
            border = BorderStroke(
                width = Dimen.PaddingXXS,
                color = if (isExpanded) InputFocusedBorderColor else CardBorderColor
            ),
            colors = CardDefaults.outlinedCardColors(containerColor = InputBackgroundColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getVehicleIcon(selectedType),
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeM)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.M))
                    Column {
                        Text(
                            text = getVehicleTitle(selectedType),
                            style = MaterialTheme.typography.s15.semiBold(),
                            color = TextPrimary
                        )
                        Text(
                            text = getVehicleDescription(selectedType),
                            style = MaterialTheme.typography.s12.normal(),
                            color = TextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(Dimen.SizeM)
                )
            }
        }

        // Expanded Dropdown List
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimen.PaddingXS)
                    .background(InputBackgroundColor, RoundedCornerShape(AppShape.ShapeM))
                    .border(
                        width = Dimen.PaddingXXS,
                        color = CardBorderColor,
                        shape = RoundedCornerShape(AppShape.ShapeM)
                    )
            ) {
                VehicleType.entries.forEach { type ->
                    val isSelected = type == selectedType
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) RoleCardSelectedBg else RoleCardUnselectedBg,
                                shape = RoundedCornerShape(AppShape.ShapeM)
                            )
                            .clickable { onSelectType(type) }
                            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingSM)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getVehicleIcon(type),
                                contentDescription = null,
                                tint = if (isSelected) PrimaryColor else TextSecondary,
                                modifier = Modifier.size(Dimen.SizeM)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.M))
                            Column {
                                Text(
                                    text = getVehicleTitle(type),
                                    style = MaterialTheme.typography.s14.semiBold(),
                                    color = if (isSelected) PrimaryColor else TextPrimary
                                )
                                Text(
                                    text = getVehicleDescription(type),
                                    style = MaterialTheme.typography.s12.normal(),
                                    color = TextSecondary
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeSM)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getVehicleIcon(type: VehicleType): ImageVector {
    return when (type) {
        VehicleType.BIKE -> Icons.Default.DirectionsBike
        VehicleType.CAR_4_SEAT -> Icons.Default.DirectionsCar
        VehicleType.CAR_7_SEAT -> Icons.Default.ElectricCar
        VehicleType.EXPRESS -> Icons.Default.LocalShipping
    }
}

@Composable
private fun getVehicleTitle(type: VehicleType): String {
    return when (type) {
        VehicleType.BIKE -> stringResource(id = R.string.vehicle_type_bike)
        VehicleType.CAR_4_SEAT -> stringResource(id = R.string.vehicle_type_car4)
        VehicleType.CAR_7_SEAT -> stringResource(id = R.string.vehicle_type_car7)
        VehicleType.EXPRESS -> stringResource(id = R.string.vehicle_type_express)
    }
}

@Composable
private fun getVehicleDescription(type: VehicleType): String {
    return when (type) {
        VehicleType.BIKE -> stringResource(id = R.string.role_driver_desc)
        VehicleType.CAR_4_SEAT -> stringResource(id = R.string.vehicle_type_car4)
        VehicleType.CAR_7_SEAT -> stringResource(id = R.string.vehicle_type_car7)
        VehicleType.EXPRESS -> stringResource(id = R.string.vehicle_type_express)
    }
}
