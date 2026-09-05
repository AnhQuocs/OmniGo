package com.example.omnigo.features.auth.presentation.ui.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.omnigo.R
import com.example.omnigo.core.components.AppButton
import com.example.omnigo.core.components.AppTextField
import com.example.omnigo.features.auth.domain.model.UserRole
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.InputBackgroundColor
import com.example.omnigo.ui.theme.InputBorderColor
import com.example.omnigo.ui.theme.InputFocusedBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.RoleCardSelectedBg
import com.example.omnigo.ui.theme.RoleCardSelectedBorder
import com.example.omnigo.ui.theme.RoleCardUnselectedBg
import com.example.omnigo.ui.theme.SuccessColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.s18
import com.example.omnigo.utils.semiBold

@Composable
fun RoleSelectionSection(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.select_role),
            style = MaterialTheme.typography.s16.semiBold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.M),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Customer Card
            OutlinedCard(
                onClick = { onRoleSelected(UserRole.CUSTOMER) },
                shape = RoundedCornerShape(AppShape.ShapeM),
                border = BorderStroke(
                    width = Dimen.PaddingXXS,
                    color = if (selectedRole == UserRole.CUSTOMER) RoleCardSelectedBorder else CardBorderColor
                ),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (selectedRole == UserRole.CUSTOMER) RoleCardSelectedBg else RoleCardUnselectedBg
                ),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(Dimen.PaddingSM)) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (selectedRole == UserRole.CUSTOMER) PrimaryColor else TextSecondary,
                        modifier = Modifier.size(Dimen.SizeL)
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XS))
                    Text(
                        text = stringResource(id = R.string.role_customer_title),
                        style = MaterialTheme.typography.s15.semiBold(),
                        color = if (selectedRole == UserRole.CUSTOMER) PrimaryColor else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = stringResource(id = R.string.role_customer_desc),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }

            // Driver Card
            OutlinedCard(
                onClick = { onRoleSelected(UserRole.DRIVER) },
                shape = RoundedCornerShape(AppShape.ShapeM),
                border = BorderStroke(
                    width = Dimen.PaddingXXS,
                    color = if (selectedRole == UserRole.DRIVER) RoleCardSelectedBorder else CardBorderColor
                ),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (selectedRole == UserRole.DRIVER) RoleCardSelectedBg else RoleCardUnselectedBg
                ),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(Dimen.PaddingSM)) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = if (selectedRole == UserRole.DRIVER) PrimaryColor else TextSecondary,
                        modifier = Modifier.size(Dimen.SizeL)
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XS))
                    Text(
                        text = stringResource(id = R.string.role_driver_title),
                        style = MaterialTheme.typography.s15.semiBold(),
                        color = if (selectedRole == UserRole.DRIVER) PrimaryColor else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.XXS))
                    Text(
                        text = stringResource(id = R.string.role_driver_desc),
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneVerificationSection(
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.step_otp),
            style = MaterialTheme.typography.s16.semiBold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        if (!isPhoneVerified) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneChange,
                    label = stringResource(id = R.string.phone_number),
                    placeholder = stringResource(id = R.string.phone_number_hint),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f)
                )

                AppButton(
                    onClick = onSendOtp,
                    isEnabled = phoneNumber.isNotBlank() && !isSendingOtp && resendCountdown == 0,
                    isLoading = isSendingOtp,
                    text = if (resendCountdown > 0) {
                        stringResource(id = R.string.resend_otp_countdown, resendCountdown)
                    } else {
                        stringResource(id = R.string.send_otp)
                    },
                    modifier = Modifier.width(Dimen.SizeUltra)
                )
            }

            AnimatedVisibility(visible = verificationId.isNotBlank()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(AppSpacing.M))

                    Text(
                        text = stringResource(id = R.string.enter_otp),
                        style = MaterialTheme.typography.s14.normal(),
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.S))

                    OtpInputField(
                        otpCode = otpCode,
                        onOtpChange = onOtpChange
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.M))

                    AppButton(
                        onClick = onVerifyOtp,
                        isEnabled = otpCode.length == 6,
                        text = stringResource(id = R.string.verify_otp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = RoleCardSelectedBg,
                        shape = RoundedCornerShape(AppShape.ShapeM)
                    )
                    .border(
                        width = Dimen.PaddingXXS,
                        color = SuccessColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(AppShape.ShapeM)
                    )
                    .padding(Dimen.PaddingSM)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessColor,
                    modifier = Modifier.size(Dimen.SizeM)
                )
                Spacer(modifier = Modifier.width(AppSpacing.S))
                Column {
                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.s15.semiBold(),
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(id = R.string.phone_verified),
                        style = MaterialTheme.typography.s12.normal(),
                        color = SuccessColor
                    )
                }
            }
        }
    }
}

@Composable
fun OtpInputField(
    otpCode: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpLength: Int = 6
) {
    BasicTextField(
        value = otpCode,
        onValueChange = { newValue ->
            if (newValue.length <= otpLength && newValue.all { it.isDigit() }) {
                onOtpChange(newValue)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.S),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(otpLength) { index ->
                    val char = otpCode.getOrNull(index)?.toString() ?: ""
                    val isFocused = otpCode.length == index
                    val isFilled = char.isNotEmpty()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimen.HeightDefault)
                            .background(
                                color = InputBackgroundColor,
                                shape = RoundedCornerShape(AppShape.ShapeM)
                            )
                            .border(
                                width = Dimen.PaddingXXS,
                                color = when {
                                    isFocused -> InputFocusedBorderColor
                                    isFilled -> PrimaryColor
                                    else -> InputBorderColor
                                },
                                shape = RoundedCornerShape(AppShape.ShapeM)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.s18.bold(),
                            color = TextPrimary
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}
