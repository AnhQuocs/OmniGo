package com.example.omnigo.features.auth.presentation.ui.register.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.omnigo.R
import com.example.omnigo.core.components.AppButton
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.OmniGoTheme
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s15
import com.example.omnigo.utils.s22
import com.example.omnigo.utils.semiBold

@Composable
fun DriverPendingApprovalScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    OmniGoTheme(darkTheme = false) {
        Scaffold(
            containerColor = BackgroundLight,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = Dimen.PaddingL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pending icon with circular background
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(Dimen.SizeMega + Dimen.SizeSM)
                        .background(PrimaryColor.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(Dimen.SizeXL)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Title
                Text(
                    text = stringResource(id = R.string.driver_pending_title),
                    style = MaterialTheme.typography.s22.bold(),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.M))

                // Description
                Text(
                    text = stringResource(id = R.string.driver_pending_desc),
                    style = MaterialTheme.typography.s15.normal(),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.L))

                // Info note card
                OutlinedCard(
                    shape = RoundedCornerShape(AppShape.ShapeM),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.driver_pending_info_note),
                        style = MaterialTheme.typography.s13.normal(),
                        color = TextPrimary,
                        modifier = Modifier.padding(Dimen.PaddingM),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.XL))

                // Button back to login
                AppButton(
                    text = stringResource(id = R.string.driver_pending_btn_login),
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
