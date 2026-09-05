package com.example.omnigo.core.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.DisabledButtonBackground
import com.example.omnigo.ui.theme.DisabledButtonContent
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = PrimaryColor,
    contentColor: Color = OnPrimaryColor,
    disabledContainerColor: Color = DisabledButtonBackground,
    disabledContentColor: Color = DisabledButtonContent,
    shape: Shape = RoundedCornerShape(AppShape.ShapeM),
    content: (@Composable RowScope.() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(Dimen.SizeSM),
                strokeWidth = Dimen.PaddingXXS
            )
        } else if (content != null) {
            content()
        } else if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.s16.semiBold(),
                modifier = Modifier.padding(vertical = Dimen.PaddingXSPlus)
            )
        }
    }
}
