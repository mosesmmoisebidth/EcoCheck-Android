package com.moses.inspectionapp.ui.screens.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.store.AppPreferences
import com.moses.inspectionapp.data.store.LocaleManager
import com.moses.inspectionapp.ui.components.AppFilterChip
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
fun LanguageScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val options = listOf(
        "en" to stringResource(R.string.language_english),
        "rw" to stringResource(R.string.language_kinyarwanda),
    )
    var selectedLanguage by remember { mutableStateOf<String?>(AppPreferences.languageCode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.large)
            .background(AppColors.PageBackground),
        verticalArrangement = Arrangement.spacedBy(Dimens.medium),
    ) {
        Text(
            text = stringResource(R.string.choose_language),
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.TextPrimary,
        )
        options.forEach { (code, label) ->
            AppFilterChip(
                label = label,
                isSelected = selectedLanguage == code,
                onClick = { selectedLanguage = code },
            )
        }
        PrimaryButton(
            text = stringResource(R.string.continue_label),
            onClick = {
                AppPreferences.languageCode = selectedLanguage
                LocaleManager.applyLanguage(selectedLanguage)
                (context as? android.app.Activity)?.recreate()
                onContinue()
            },
            enabled = selectedLanguage != null,
        )
    }
}
