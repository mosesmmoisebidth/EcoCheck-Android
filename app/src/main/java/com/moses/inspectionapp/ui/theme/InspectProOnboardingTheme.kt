package com.moses.inspectionapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.GoogleFont.Provider
import androidx.compose.ui.unit.sp
import com.moses.inspectionapp.R

object InspectProColors {
    val Background = Color(0xFF080E1A)
    val Surface = Color(0xFF0C1322)
    val Accent = Color(0xFF5B9BFF)
    val GradientStart = Color(0xFF2563EB)
    val GradientEnd = Color(0xFF5B9BFF)
    val TextPrimary = Color(0xFFEDF2FF)
    val TextSecondary = Color(0xFF6B80A8)
    val TextMuted = Color(0xFF99AACC)
    val Success = Color(0xFF22C55E)
    val StatCardBg = Color(0x0AFFFFFF)
    val StatCardBorder = Color(0x12FFFFFF)
    val Divider = Color(0x10FFFFFF)
    val SkipBg = Color(0x12FFFFFF)
    val SkipBorder = Color(0x1FFFFFFF)
    val BackBtnBg = Color(0x0DFFFFFF)
    val BackBtnBorder = Color(0x1AFFFFFF)
    val DotActive = Color(0xFF5B9BFF)
    val DotInactive = Color(0x26FFFFFF)
}

private val googleFontProvider = Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

val SoraFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Sora"),
        fontProvider = googleFontProvider,
        weight = FontWeight.ExtraBold,
    ),
    Font(
        googleFont = GoogleFont("Sora"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Bold,
    ),
    Font(
        googleFont = GoogleFont("Sora"),
        fontProvider = googleFontProvider,
        weight = FontWeight.SemiBold,
    ),
    Font(
        googleFont = GoogleFont("Sora"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Medium,
    ),
)

val DMSansFamily = FontFamily(
    Font(
        googleFont = GoogleFont("DM Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Medium,
    ),
    Font(
        googleFont = GoogleFont("DM Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal,
    ),
)

private val InspectProTypography = androidx.compose.material3.Typography(
    titleSmall = TextStyle(
        fontFamily = SoraFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 15.sp,
        lineHeight = 18.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = SoraFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = DMSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = DMSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

@Composable
fun InspectProOnboardingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = InspectProColors.Background,
            surface = InspectProColors.Surface,
            primary = InspectProColors.Accent,
        ),
        typography = InspectProTypography,
        content = content,
    )
}
