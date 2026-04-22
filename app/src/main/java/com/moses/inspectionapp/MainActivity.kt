package com.moses.inspectionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.AppPreferences
import com.moses.inspectionapp.data.store.LocaleManager
import com.moses.inspectionapp.ui.theme.InspectionAppTheme
import com.moses.inspectionapp.ui.navigation.AppNavHost
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.ProvideResponsiveTokens
import androidx.compose.ui.graphics.toArgb

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.init(applicationContext)
        LocaleManager.applyLanguage(AppPreferences.languageCode)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AppColors.NavyDark.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            InspectionAppTheme {
                ProvideResponsiveTokens(windowSizeClass = windowSizeClass) {
                    AppNavHost(windowSizeClass = windowSizeClass)
                }
            }
        }
    }
}
