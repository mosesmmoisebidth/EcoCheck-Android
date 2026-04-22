package com.moses.inspectionapp.ui.theme

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class AppSpacing(
    val screenPaddingH: Dp,
    val screenPaddingV: Dp,
    val cardPadding: Dp,
    val itemSpacing: Dp,
    val sectionSpacing: Dp,
    val iconSize: Dp,
    val buttonHeight: Dp,
    val inputHeight: Dp,
    val avatarSize: Dp,
    val stepDotSize: Dp,
    val cardMaxWidth: Dp,
)

@Immutable
data class AppTypography(
    val displayLarge: TextUnit,
    val titleLarge: TextUnit,
    val titleMedium: TextUnit,
    val bodyLarge: TextUnit,
    val bodyMedium: TextUnit,
    val labelSmall: TextUnit,
)

private val CompactSpacing = AppSpacing(
    screenPaddingH = 20.dp,
    screenPaddingV = 16.dp,
    cardPadding = 16.dp,
    itemSpacing = 12.dp,
    sectionSpacing = 20.dp,
    iconSize = 24.dp,
    buttonHeight = 52.dp,
    inputHeight = 52.dp,
    avatarSize = 56.dp,
    stepDotSize = 28.dp,
    cardMaxWidth = 560.dp,
)

private val MediumSpacing = AppSpacing(
    screenPaddingH = 32.dp,
    screenPaddingV = 24.dp,
    cardPadding = 20.dp,
    itemSpacing = 16.dp,
    sectionSpacing = 28.dp,
    iconSize = 26.dp,
    buttonHeight = 56.dp,
    inputHeight = 56.dp,
    avatarSize = 64.dp,
    stepDotSize = 32.dp,
    cardMaxWidth = 640.dp,
)

private val ExpandedSpacing = AppSpacing(
    screenPaddingH = 48.dp,
    screenPaddingV = 32.dp,
    cardPadding = 24.dp,
    itemSpacing = 20.dp,
    sectionSpacing = 36.dp,
    iconSize = 28.dp,
    buttonHeight = 60.dp,
    inputHeight = 60.dp,
    avatarSize = 72.dp,
    stepDotSize = 36.dp,
    cardMaxWidth = 720.dp,
)

private val CompactTypography = AppTypography(
    displayLarge = 26.sp,
    titleLarge = 20.sp,
    titleMedium = 16.sp,
    bodyLarge = 15.sp,
    bodyMedium = 13.sp,
    labelSmall = 11.sp,
)

private val MediumTypography = AppTypography(
    displayLarge = 30.sp,
    titleLarge = 24.sp,
    titleMedium = 18.sp,
    bodyLarge = 16.sp,
    bodyMedium = 14.sp,
    labelSmall = 12.sp,
)

private val ExpandedTypography = AppTypography(
    displayLarge = 34.sp,
    titleLarge = 28.sp,
    titleMedium = 20.sp,
    bodyLarge = 17.sp,
    bodyMedium = 15.sp,
    labelSmall = 13.sp,
)

val LocalAppSpacing = staticCompositionLocalOf { CompactSpacing }
val LocalAppTypography = staticCompositionLocalOf { CompactTypography }

private fun spacingFor(windowSizeClass: WindowSizeClass): AppSpacing {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactSpacing
        WindowWidthSizeClass.Medium -> MediumSpacing
        else -> ExpandedSpacing
    }
}

private fun typographyFor(windowSizeClass: WindowSizeClass): AppTypography {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactTypography
        WindowWidthSizeClass.Medium -> MediumTypography
        else -> ExpandedTypography
    }
}

@Composable
fun ProvideResponsiveTokens(
    windowSizeClass: WindowSizeClass,
    content: @Composable () -> Unit,
) {
    val spacing = spacingFor(windowSizeClass)
    val typography = typographyFor(windowSizeClass)
    SideEffect { Dimens.updateFromSpacing(spacing) }
    CompositionLocalProvider(
        LocalAppSpacing provides spacing,
        LocalAppTypography provides typography,
        content = content,
    )
}

