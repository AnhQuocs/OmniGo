package com.example.omnigo.features.language.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.omnigo.BaseComponentActivity
import com.example.omnigo.R
import com.example.omnigo.features.language.domain.model.AppLanguage
import com.example.omnigo.features.language.presentation.viewmodel.LanguageViewModel
import com.example.omnigo.features.main.MainActivity
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.utils.LangUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeLanguageActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ChangeLanguageScreen(
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun ChangeLanguageScreen(
    onBackClick: () -> Unit,
    languageViewModel: LanguageViewModel = hiltViewModel()
) {
    val currentLang by languageViewModel.currentLanguage.collectAsState()
    var selectedLang by remember(currentLang) { mutableStateOf(currentLang) }

    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimen.PaddingM, vertical = Dimen.PaddingXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = Dimen.PaddingL, start = Dimen.PaddingSM)
                    .size(Dimen.SizeL)
                    .clickable { onBackClick() }
            )

            Text(
                stringResource(id = R.string.select_language),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = Dimen.PaddingL)
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.LPlus))

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(AppShape.ShapeL))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .fillMaxWidth()
                .padding(Dimen.PaddingM)
        ) {
            LanguageOption(
                title = stringResource(id = R.string.english) + " 🇬🇧",
                isSelected = selectedLang == AppLanguage.ENGLISH,
                onClick = { selectedLang = AppLanguage.ENGLISH }
            )

            LanguageOption(
                title = stringResource(id = R.string.vietnamese) + " 🇻🇳",
                isSelected = selectedLang == AppLanguage.VIETNAMESE,
                onClick = { selectedLang = AppLanguage.VIETNAMESE }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                val code = selectedLang.code
                LangUtils.updateLocale(context, code)
                languageViewModel.changeLanguage(selectedLang)

                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                activity?.overridePendingTransition(0, 0)
            },
            shape = RoundedCornerShape(AppShape.ShapeL),
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.HeightDefault)
        ) {
            Text(stringResource(id = R.string.apply), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun LanguageOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onBackground
            )
        )

        RadioButton(
            selected = isSelected,
            onClick = { onClick() },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}