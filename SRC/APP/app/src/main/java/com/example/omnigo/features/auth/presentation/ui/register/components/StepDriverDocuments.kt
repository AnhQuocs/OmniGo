package com.example.omnigo.features.auth.presentation.ui.register.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.omnigo.R
import com.example.omnigo.core.components.AppTextField
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.utils.s16
import com.example.omnigo.utils.semiBold

@Composable
fun StepDriverDocuments(
    docsState: DriverDocsState,
    modifier: Modifier = Modifier
) {
    val cccdFrontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { docsState.onCccdFrontSelected(it) }
    }

    val cccdBackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { docsState.onCccdBackSelected(it) }
    }

    val gplxFrontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { docsState.onGplxFrontSelected(it) }
    }

    val gplxBackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { docsState.onGplxBackSelected(it) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // --- CCCD SECTION ---
        Text(
            text = stringResource(id = R.string.driver_doc_cccd_title),
            style = MaterialTheme.typography.s16.semiBold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        AppTextField(
            value = docsState.cccdNumber,
            onValueChange = { docsState.cccdNumber = it },
            label = stringResource(id = R.string.driver_cccd_number),
            placeholder = stringResource(id = R.string.driver_cccd_number_hint),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        DocumentUploadCard(
            title = stringResource(id = R.string.driver_doc_cccd_front),
            localUri = docsState.cccdFrontUri,
            imageUrl = docsState.cccdFrontUrl,
            isUploading = docsState.isCccdFrontUploading,
            onPickImage = { cccdFrontLauncher.launch("image/*") },
            onClearImage = { docsState.clearCccdFront() }
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        DocumentUploadCard(
            title = stringResource(id = R.string.driver_doc_cccd_back),
            localUri = docsState.cccdBackUri,
            imageUrl = docsState.cccdBackUrl,
            isUploading = docsState.isCccdBackUploading,
            onPickImage = { cccdBackLauncher.launch("image/*") },
            onClearImage = { docsState.clearCccdBack() }
        )

        Spacer(modifier = Modifier.height(AppSpacing.L))

        // --- GPLX SECTION ---
        Text(
            text = stringResource(id = R.string.driver_doc_gplx_title),
            style = MaterialTheme.typography.s16.semiBold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        AppTextField(
            value = docsState.gplxNumber,
            onValueChange = { docsState.gplxNumber = it },
            label = stringResource(id = R.string.driver_gplx_number),
            placeholder = stringResource(id = R.string.driver_gplx_number_hint),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        DocumentUploadCard(
            title = stringResource(id = R.string.driver_doc_gplx_front),
            localUri = docsState.gplxFrontUri,
            imageUrl = docsState.gplxFrontUrl,
            isUploading = docsState.isGplxFrontUploading,
            onPickImage = { gplxFrontLauncher.launch("image/*") },
            onClearImage = { docsState.clearGplxFront() }
        )

        Spacer(modifier = Modifier.height(AppSpacing.M))

        DocumentUploadCard(
            title = stringResource(id = R.string.driver_doc_gplx_back),
            localUri = docsState.gplxBackUri,
            imageUrl = docsState.gplxBackUrl,
            isUploading = docsState.isGplxBackUploading,
            onPickImage = { gplxBackLauncher.launch("image/*") },
            onClearImage = { docsState.clearGplxBack() }
        )
    }
}
