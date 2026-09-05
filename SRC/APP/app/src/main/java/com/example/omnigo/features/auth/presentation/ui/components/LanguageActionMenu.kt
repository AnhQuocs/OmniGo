package com.example.omnigo.features.auth.presentation.ui.components

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.omnigo.R
import com.example.omnigo.features.language.domain.model.AppLanguage
import com.example.omnigo.features.language.presentation.viewmodel.LanguageViewModel
import com.example.omnigo.features.main.MainActivity
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.utils.LangUtils
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.semiBold

@Composable
fun LanguageActionMenu(
    modifier: Modifier = Modifier,
    languageViewModel: LanguageViewModel = hiltViewModel()
) {
    val currentLang by languageViewModel.currentLanguage.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(AppShape.ShapeM))
                .background(SurfaceLight)
                .border(
                    width = Dimen.PaddingXXS,
                    color = CardBorderColor,
                    shape = RoundedCornerShape(AppShape.ShapeM)
                )
                .clickable { isExpanded = true }
                .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingXS)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(Dimen.SizeS)
            )

            Spacer(modifier = Modifier.width(AppSpacing.XS))

            Text(
                text = if (currentLang == AppLanguage.VIETNAMESE) "VI" else "EN",
                style = MaterialTheme.typography.s13.semiBold(),
                color = TextPrimary
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(Dimen.SizeS)
            )
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(SurfaceLight)
        ) {
            AppLanguage.entries.forEach { lang ->
                val isSelected = currentLang == lang
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ENGLISH -> stringResource(id = R.string.english) + " 🇬🇧"
                                    AppLanguage.VIETNAMESE -> stringResource(id = R.string.vietnamese) + " 🇻🇳"
                                },
                                style = MaterialTheme.typography.s14.semiBold(),
                                color = if (isSelected) PrimaryColor else TextPrimary
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(AppSpacing.S))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(Dimen.SizeS)
                                )
                            }
                        }
                    },
                    onClick = {
                        isExpanded = false
                        if (currentLang != lang) {
                            LangUtils.updateLocale(context, lang.code)
                            languageViewModel.changeLanguage(lang)

                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            activity?.overridePendingTransition(0, 0)
                        }
                    }
                )
            }
        }
    }
}
