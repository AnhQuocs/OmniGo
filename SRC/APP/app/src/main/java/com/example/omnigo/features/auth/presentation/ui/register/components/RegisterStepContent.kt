package com.example.omnigo.features.auth.presentation.ui.register.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import com.example.omnigo.features.auth.domain.model.UserRole
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.auth.presentation.ui.register.AccountInfoSection
import com.example.omnigo.features.auth.presentation.ui.register.DriverInfoSection
import com.example.omnigo.features.auth.presentation.ui.register.PhoneVerificationSection
import com.example.omnigo.features.auth.presentation.ui.register.RoleSelectionSection
import com.example.omnigo.ui.dimens.AppSpacing

@Composable
fun RegisterStepContent(
    step: Int,
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    verificationId: String,
    isPhoneVerified: Boolean,
    isSendingOtp: Boolean,
    resendCountdown: Int,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
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
    focusManager: FocusManager,
    selectedVehicleType: VehicleType,
    onVehicleTypeSelected: (VehicleType) -> Unit,
    isVehicleDropdownExpanded: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit,
    licensePlate: String,
    onLicensePlateChange: (String) -> Unit,
    vehicleModel: String,
    onVehicleModelChange: (String) -> Unit,
    docsState: DriverDocsState,
    modifier: Modifier = Modifier
) {
    when (step) {
        0 -> RoleSelectionSection(
            selectedRole = selectedRole,
            onRoleSelected = onRoleSelected,
            modifier = modifier
        )
        1 -> PhoneVerificationSection(
            phoneNumber = phoneNumber,
            onPhoneChange = onPhoneChange,
            otpCode = otpCode,
            onOtpChange = onOtpChange,
            verificationId = verificationId,
            isPhoneVerified = isPhoneVerified,
            isSendingOtp = isSendingOtp,
            resendCountdown = resendCountdown,
            onSendOtp = onSendOtp,
            onVerifyOtp = onVerifyOtp,
            modifier = modifier
        )
        2 -> Column(modifier = modifier.fillMaxWidth()) {
            AccountInfoSection(
                fullName = fullName,
                onFullNameChange = onFullNameChange,
                email = email,
                onEmailChange = onEmailChange,
                password = password,
                onPasswordChange = onPasswordChange,
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = onConfirmPasswordChange,
                isPasswordVisible = isPasswordVisible,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                isConfirmPasswordVisible = isConfirmPasswordVisible,
                onToggleConfirmPasswordVisibility = onToggleConfirmPasswordVisibility,
                isCustomer = selectedRole == UserRole.CUSTOMER,
                focusManager = focusManager
            )
            Spacer(modifier = Modifier.height(AppSpacing.S))
            PasswordStrengthMeter(password = password)
        }
        3 -> DriverInfoSection(
            selectedVehicleType = selectedVehicleType,
            onVehicleTypeSelected = onVehicleTypeSelected,
            isDropdownExpanded = isVehicleDropdownExpanded,
            onDropdownExpandedChange = onDropdownExpandedChange,
            licensePlate = licensePlate,
            onLicensePlateChange = onLicensePlateChange,
            vehicleModel = vehicleModel,
            onVehicleModelChange = onVehicleModelChange,
            focusManager = focusManager,
            modifier = modifier
        )
        4 -> StepDriverDocuments(
            docsState = docsState,
            modifier = modifier
        )
    }
}
