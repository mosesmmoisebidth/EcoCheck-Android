package com.moses.inspectionapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    // PRIMARY PALETTE — Navy + Steel Blue (WISE PBM style)
    val NavyDark = Color(0xFF0D2B5E)
    val NavyMid = Color(0xFF1A3A7A)
    val SteelBlue = Color(0xFF1F5BB4)
    val SteelBlueLight = Color(0xFF2E70D4)
    val SteelBlueTint = Color(0xFFE8EFF9)
    val SteelBlueTintMid = Color(0xFFD0DFFA)

    // BACKGROUNDS
    val PageBackground = Color(0xFFEFF3FA)
    val CardSurface = Color(0xFFFFFFFF)
    val InputSurface = Color(0xFFF4F7FC)

    // PRESS / RIPPLE / HOVER STATES
    val PressedCard = Color(0xFFD6E4F7)
    val PressedCardBorder = Color(0xFF1F5BB4)
    val RippleColor = Color(0xFF1F5BB4)
    val SelectedRowBg = Color(0xFFDEEAFA)
    val SelectedRowBorder = Color(0xFF1F5BB4)
    val HoverChip = Color(0xFFBDD4F5)
    val PressedButton = Color(0xFF184A9A)
    val FocusedInput = Color(0xFFEBF2FF)

    // ACCENT COLORS (functional, used sparingly)
    val AccentOrange = Color(0xFFE8650A)
    val AccentOrangePress = Color(0xFFBF4E06)
    val AccentGold = Color(0xFFF5A623)
    val AccentGreen = Color(0xFF1B7A3E)
    val AccentGreenBg = Color(0xFFE6F4EC)
    val AccentRed = Color(0xFFC0392B)
    val AccentRedBg = Color(0xFFFDECEB)
    val AccentRedPress = Color(0xFF9B2B20)

    // STATUS COLORS (inspection decisions)
    val StatusCompliant = Color(0xFF1B7A3E)
    val StatusCompliantBg = Color(0xFFE6F4EC)
    val StatusCompliantPress = Color(0xFFCCEDD8)
    val StatusWarning = Color(0xFFB45309)
    val StatusWarningBg = Color(0xFFFEF3C7)
    val StatusWarningPress = Color(0xFFFDE68A)
    val StatusClosure = Color(0xFFE8650A)
    val StatusClosureBg = Color(0xFFFEF0E6)
    val StatusClosurePress = Color(0xFFFDD5B4)
    val StatusImmediate = Color(0xFFC0392B)
    val StatusImmediateBg = Color(0xFFFDECEB)
    val StatusImmediatePress = Color(0xFFFBC8C8)
    val StatusProsecution = Color(0xFF6B21A8)
    val StatusProsecutionBg = Color(0xFFF3E8FF)
    val StatusProsecutionPress = Color(0xFFE4CCFF)

    // SYNC STATUS
    val SyncPending = Color(0xFFE8650A)
    val SyncPendingBg = Color(0xFFFEF0E6)
    val SyncSynced = Color(0xFF1B7A3E)
    val SyncSyncedBg = Color(0xFFE6F4EC)
    val SyncFailed = Color(0xFFC0392B)
    val SyncFailedBg = Color(0xFFFDECEB)

    // TEXT
    val TextPrimary = Color(0xFF0F1F3D)
    val TextSecondary = Color(0xFF5A6F8C)
    val TextHint = Color(0xFF9AADC4)
    val TextOnDark = Color(0xFFFFFFFF)
    val TextOnDarkMuted = Color(0xFFB8CCE8)

    // BORDERS & DIVIDERS
    val BorderLight = Color(0xFFDDE6F0)
    val BorderMedium = Color(0xFFBDCEE0)
    val Divider = Color(0xFFEAF0F8)
}

private val AppColorScheme = lightColorScheme(
    primary = AppColors.SteelBlue,
    onPrimary = AppColors.TextOnDark,
    secondary = AppColors.SteelBlueLight,
    onSecondary = AppColors.TextOnDark,
    background = AppColors.PageBackground,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.CardSurface,
    onSurface = AppColors.TextPrimary,
    error = AppColors.AccentRed,
    outline = AppColors.BorderLight,
)

@Composable
fun InspectionAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
