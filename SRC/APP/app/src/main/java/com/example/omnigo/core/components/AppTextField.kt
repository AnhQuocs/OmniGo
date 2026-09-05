package com.example.omnigo.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.theme.ErrorColor
import com.example.omnigo.ui.theme.InputBackgroundColor
import com.example.omnigo.ui.theme.InputBorderColor
import com.example.omnigo.ui.theme.InputFocusedBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s14

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    shape: Shape = RoundedCornerShape(AppShape.ShapeM)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label?.let {
            { Text(text = it, style = MaterialTheme.typography.s14.normal()) }
        },
        placeholder = placeholder?.let {
            { Text(text = it, style = MaterialTheme.typography.s14.normal()) }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        enabled = enabled,
        readOnly = readOnly,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = InputBackgroundColor,
            unfocusedContainerColor = InputBackgroundColor,
            focusedBorderColor = InputFocusedBorderColor,
            unfocusedBorderColor = InputBorderColor,
            focusedLabelColor = PrimaryColor,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = PrimaryColor,
            errorBorderColor = ErrorColor,
            errorLabelColor = ErrorColor
        ),
        modifier = modifier.fillMaxWidth()
    )
}
