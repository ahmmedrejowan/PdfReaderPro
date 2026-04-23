package com.rejowan.pdfreaderpro.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * CompositionLocal to access the current dark mode state throughout the app.
 */
val LocalIsDarkTheme = staticCompositionLocalOf { true }

/**
 * Available theme modes for the app.
 */
enum class ThemeMode(val displayName: String) {
    DARK("Dark"),
    LIGHT("Light"),
    BLACK("Black"),
    SYSTEM("System Default")
}

// ============================================================================
// DARK COLOR SCHEME
// ============================================================================

private val darkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = DarkSurfaces.error,
    onError = DarkSurfaces.onError,
    errorContainer = DarkSurfaces.errorContainer,
    onErrorContainer = DarkSurfaces.onErrorContainer,
    background = DarkSurfaces.background,
    onBackground = DarkSurfaces.onBackground,
    surface = DarkSurfaces.surface,
    onSurface = DarkSurfaces.onSurface,
    surfaceVariant = DarkSurfaces.surfaceVariant,
    onSurfaceVariant = DarkSurfaces.onSurfaceVariant,
    outline = DarkSurfaces.outline,
    outlineVariant = DarkSurfaces.outlineVariant,
    scrim = DarkSurfaces.scrim,
    inverseSurface = DarkSurfaces.inverseSurface,
    inverseOnSurface = DarkSurfaces.inverseOnSurface,
    inversePrimary = inversePrimaryDark,
    surfaceDim = DarkSurfaces.surfaceDim,
    surfaceBright = DarkSurfaces.surfaceBright,
    surfaceContainerLowest = DarkSurfaces.surfaceContainerLowest,
    surfaceContainerLow = DarkSurfaces.surfaceContainerLow,
    surfaceContainer = DarkSurfaces.surfaceContainer,
    surfaceContainerHigh = DarkSurfaces.surfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaces.surfaceContainerHighest,
)

// ============================================================================
// BLACK (AMOLED) COLOR SCHEME — reuses dark palette over true-black surfaces
// ============================================================================

private val blackColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = BlackSurfaces.error,
    onError = BlackSurfaces.onError,
    errorContainer = BlackSurfaces.errorContainer,
    onErrorContainer = BlackSurfaces.onErrorContainer,
    background = BlackSurfaces.background,
    onBackground = BlackSurfaces.onBackground,
    surface = BlackSurfaces.surface,
    onSurface = BlackSurfaces.onSurface,
    surfaceVariant = BlackSurfaces.surfaceVariant,
    onSurfaceVariant = BlackSurfaces.onSurfaceVariant,
    outline = BlackSurfaces.outline,
    outlineVariant = BlackSurfaces.outlineVariant,
    scrim = BlackSurfaces.scrim,
    inverseSurface = BlackSurfaces.inverseSurface,
    inverseOnSurface = BlackSurfaces.inverseOnSurface,
    inversePrimary = inversePrimaryDark,
    surfaceDim = BlackSurfaces.surfaceDim,
    surfaceBright = BlackSurfaces.surfaceBright,
    surfaceContainerLowest = BlackSurfaces.surfaceContainerLowest,
    surfaceContainerLow = BlackSurfaces.surfaceContainerLow,
    surfaceContainer = BlackSurfaces.surfaceContainer,
    surfaceContainerHigh = BlackSurfaces.surfaceContainerHigh,
    surfaceContainerHighest = BlackSurfaces.surfaceContainerHighest,
)

// ============================================================================
// LIGHT COLOR SCHEME
// ============================================================================

private val lightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = LightSurfaces.error,
    onError = LightSurfaces.onError,
    errorContainer = LightSurfaces.errorContainer,
    onErrorContainer = LightSurfaces.onErrorContainer,
    background = LightSurfaces.background,
    onBackground = LightSurfaces.onBackground,
    surface = LightSurfaces.surface,
    onSurface = LightSurfaces.onSurface,
    surfaceVariant = LightSurfaces.surfaceVariant,
    onSurfaceVariant = LightSurfaces.onSurfaceVariant,
    outline = LightSurfaces.outline,
    outlineVariant = LightSurfaces.outlineVariant,
    scrim = LightSurfaces.scrim,
    inverseSurface = LightSurfaces.inverseSurface,
    inverseOnSurface = LightSurfaces.inverseOnSurface,
    inversePrimary = inversePrimaryLight,
    surfaceDim = LightSurfaces.surfaceDim,
    surfaceBright = LightSurfaces.surfaceBright,
    surfaceContainerLowest = LightSurfaces.surfaceContainerLowest,
    surfaceContainerLow = LightSurfaces.surfaceContainerLow,
    surfaceContainer = LightSurfaces.surfaceContainer,
    surfaceContainerHigh = LightSurfaces.surfaceContainerHigh,
    surfaceContainerHighest = LightSurfaces.surfaceContainerHighest,
)

/**
 * PDF Reader Pro Theme - Supports dark, light, and system theme modes.
 */
@Composable
fun PdfReaderProTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.BLACK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemInDarkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        themeMode == ThemeMode.BLACK -> blackColorScheme
        isDarkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides isDarkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
