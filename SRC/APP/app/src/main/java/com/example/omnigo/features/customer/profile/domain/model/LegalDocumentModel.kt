package com.example.omnigo.features.customer.profile.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.omnigo.R

enum class LegalTabType {
    TERMS_OF_SERVICE,
    PRIVACY_POLICY
}

data class LegalSection(
    val icon: ImageVector,
    val titleRes: Int,
    val contentRes: Int
)

object LegalContentProvider {

    val termsOfServiceSections: List<LegalSection> = listOf(
        LegalSection(
            icon = Icons.Default.Person,
            titleRes = R.string.legal_tos_sec1_title,
            contentRes = R.string.legal_tos_sec1_content
        ),
        LegalSection(
            icon = Icons.Default.DirectionsCar,
            titleRes = R.string.legal_tos_sec2_title,
            contentRes = R.string.legal_tos_sec2_content
        ),
        LegalSection(
            icon = Icons.Default.Fastfood,
            titleRes = R.string.legal_tos_sec3_title,
            contentRes = R.string.legal_tos_sec3_content
        ),
        LegalSection(
            icon = Icons.Default.AccountBalanceWallet,
            titleRes = R.string.legal_tos_sec4_title,
            contentRes = R.string.legal_tos_sec4_content
        ),
        LegalSection(
            icon = Icons.Default.Block,
            titleRes = R.string.legal_tos_sec5_title,
            contentRes = R.string.legal_tos_sec5_content
        ),
        LegalSection(
            icon = Icons.Default.Gavel,
            titleRes = R.string.legal_tos_sec6_title,
            contentRes = R.string.legal_tos_sec6_content
        )
    )

    val privacyPolicySections: List<LegalSection> = listOf(
        LegalSection(
            icon = Icons.Default.Storage,
            titleRes = R.string.legal_privacy_sec1_title,
            contentRes = R.string.legal_privacy_sec1_content
        ),
        LegalSection(
            icon = Icons.Default.Rule,
            titleRes = R.string.legal_privacy_sec2_title,
            contentRes = R.string.legal_privacy_sec2_content
        ),
        LegalSection(
            icon = Icons.Default.Share,
            titleRes = R.string.legal_privacy_sec3_title,
            contentRes = R.string.legal_privacy_sec3_content
        ),
        LegalSection(
            icon = Icons.Default.ManageAccounts,
            titleRes = R.string.legal_privacy_sec4_title,
            contentRes = R.string.legal_privacy_sec4_content
        ),
        LegalSection(
            icon = Icons.Default.Lock,
            titleRes = R.string.legal_privacy_sec5_title,
            contentRes = R.string.legal_privacy_sec5_content
        )
    )
}

