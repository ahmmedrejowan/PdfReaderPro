package com.rejowan.pdfreaderpro.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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
 * Available accent color themes for the app.
 */
enum class AccentColor(val displayName: String) {
    DEFAULT("Purple"),
    OCEAN("Ocean"),
    SUNSET("Sunset"),
    FOREST("Forest"),
    ROSE("Rose")
}

/**
 * Available theme modes for the app.
 */
enum class ThemeMode(val displayName: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System Default")
}

// ============================================================================
// DARK COLOR SCHEMES
// ============================================================================

private val defaultDarkScheme = darkColorScheme(
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

private val oceanDarkScheme = darkColorScheme(
    primary = OceanColors.primary,
    onPrimary = OceanColors.onPrimary,
    primaryContainer = OceanColors.primaryContainer,
    onPrimaryContainer = OceanColors.onPrimaryContainer,
    secondary = OceanColors.secondary,
    onSecondary = OceanColors.onSecondary,
    secondaryContainer = OceanColors.secondaryContainer,
    onSecondaryContainer = OceanColors.onSecondaryContainer,
    tertiary = OceanColors.tertiary,
    onTertiary = OceanColors.onTertiary,
    tertiaryContainer = OceanColors.tertiaryContainer,
    onTertiaryContainer = OceanColors.onTertiaryContainer,
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
    inversePrimary = OceanColors.primaryContainer,
)

private val sunsetDarkScheme = darkColorScheme(
    primary = SunsetColors.primary,
    onPrimary = SunsetColors.onPrimary,
    primaryContainer = SunsetColors.primaryContainer,
    onPrimaryContainer = SunsetColors.onPrimaryContainer,
    secondary = SunsetColors.secondary,
    onSecondary = SunsetColors.onSecondary,
    secondaryContainer = SunsetColors.secondaryContainer,
    onSecondaryContainer = SunsetColors.onSecondaryContainer,
    tertiary = SunsetColors.tertiary,
    onTertiary = SunsetColors.onTertiary,
    tertiaryContainer = SunsetColors.tertiaryContainer,
    onTertiaryContainer = SunsetColors.onTertiaryContainer,
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
    inversePrimary = SunsetColors.primaryContainer,
)

private val forestDarkScheme = darkColorScheme(
    primary = ForestColors.primary,
    onPrimary = ForestColors.onPrimary,
    primaryContainer = ForestColors.primaryContainer,
    onPrimaryContainer = ForestColors.onPrimaryContainer,
    secondary = ForestColors.secondary,
    onSecondary = ForestColors.onSecondary,
    secondaryContainer = ForestColors.secondaryContainer,
    onSecondaryContainer = ForestColors.onSecondaryContainer,
    tertiary = ForestColors.tertiary,
    onTertiary = ForestColors.onTertiary,
    tertiaryContainer = ForestColors.tertiaryContainer,
    onTertiaryContainer = ForestColors.onTertiaryContainer,
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
    inversePrimary = ForestColors.primaryContainer,
)

private val roseDarkScheme = darkColorScheme(
    primary = RoseColors.primary,
    onPrimary = RoseColors.onPrimary,
    primaryContainer = RoseColors.primaryContainer,
    onPrimaryContainer = RoseColors.onPrimaryContainer,
    secondary = RoseColors.secondary,
    onSecondary = RoseColors.onSecondary,
    secondaryContainer = RoseColors.secondaryContainer,
    onSecondaryContainer = RoseColors.onSecondaryContainer,
    tertiary = RoseColors.tertiary,
    onTertiary = RoseColors.onTertiary,
    tertiaryContainer = RoseColors.tertiaryContainer,
    onTertiaryContainer = RoseColors.onTertiaryContainer,
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
    inversePrimary = RoseColors.primaryContainer,
)

// ============================================================================
// LIGHT COLOR SCHEMES
// ============================================================================

private val defaultLightScheme = lightColorScheme(
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

private val oceanLightScheme = lightColorScheme(
    primary = OceanColorsLight.primary,
    onPrimary = OceanColorsLight.onPrimary,
    primaryContainer = OceanColorsLight.primaryContainer,
    onPrimaryContainer = OceanColorsLight.onPrimaryContainer,
    secondary = OceanColorsLight.secondary,
    onSecondary = OceanColorsLight.onSecondary,
    secondaryContainer = OceanColorsLight.secondaryContainer,
    onSecondaryContainer = OceanColorsLight.onSecondaryContainer,
    tertiary = OceanColorsLight.tertiary,
    onTertiary = OceanColorsLight.onTertiary,
    tertiaryContainer = OceanColorsLight.tertiaryContainer,
    onTertiaryContainer = OceanColorsLight.onTertiaryContainer,
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
    inversePrimary = OceanColorsLight.primaryContainer,
)

private val sunsetLightScheme = lightColorScheme(
    primary = SunsetColorsLight.primary,
    onPrimary = SunsetColorsLight.onPrimary,
    primaryContainer = SunsetColorsLight.primaryContainer,
    onPrimaryContainer = SunsetColorsLight.onPrimaryContainer,
    secondary = SunsetColorsLight.secondary,
    onSecondary = SunsetColorsLight.onSecondary,
    secondaryContainer = SunsetColorsLight.secondaryContainer,
    onSecondaryContainer = SunsetColorsLight.onSecondaryContainer,
    tertiary = SunsetColorsLight.tertiary,
    onTertiary = SunsetColorsLight.onTertiary,
    tertiaryContainer = SunsetColorsLight.tertiaryContainer,
    onTertiaryContainer = SunsetColorsLight.onTertiaryContainer,
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
    inversePrimary = SunsetColorsLight.primaryContainer,
)

private val forestLightScheme = lightColorScheme(
    primary = ForestColorsLight.primary,
    onPrimary = ForestColorsLight.onPrimary,
    primaryContainer = ForestColorsLight.primaryContainer,
    onPrimaryContainer = ForestColorsLight.onPrimaryContainer,
    secondary = ForestColorsLight.secondary,
    onSecondary = ForestColorsLight.onSecondary,
    secondaryContainer = ForestColorsLight.secondaryContainer,
    onSecondaryContainer = ForestColorsLight.onSecondaryContainer,
    tertiary = ForestColorsLight.tertiary,
    onTertiary = ForestColorsLight.onTertiary,
    tertiaryContainer = ForestColorsLight.tertiaryContainer,
    onTertiaryContainer = ForestColorsLight.onTertiaryContainer,
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
    inversePrimary = ForestColorsLight.primaryContainer,
)

private val roseLightScheme = lightColorScheme(
    primary = RoseColorsLight.primary,
    onPrimary = RoseColorsLight.onPrimary,
    primaryContainer = RoseColorsLight.primaryContainer,
    onPrimaryContainer = RoseColorsLight.onPrimaryContainer,
    secondary = RoseColorsLight.secondary,
    onSecondary = RoseColorsLight.onSecondary,
    secondaryContainer = RoseColorsLight.secondaryContainer,
    onSecondaryContainer = RoseColorsLight.onSecondaryContainer,
    tertiary = RoseColorsLight.tertiary,
    onTertiary = RoseColorsLight.onTertiary,
    tertiaryContainer = RoseColorsLight.tertiaryContainer,
    onTertiaryContainer = RoseColorsLight.onTertiaryContainer,
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
    inversePrimary = RoseColorsLight.primaryContainer,
)

/**
 * Get color scheme for specified accent color and theme mode
 */
fun getColorScheme(accentColor: AccentColor, isDark: Boolean): ColorScheme {
    return when (accentColor) {
        AccentColor.DEFAULT -> if (isDark) defaultDarkScheme else defaultLightScheme
        AccentColor.OCEAN -> if (isDark) oceanDarkScheme else oceanLightScheme
        AccentColor.SUNSET -> if (isDark) sunsetDarkScheme else sunsetLightScheme
        AccentColor.FOREST -> if (isDark) forestDarkScheme else forestLightScheme
        AccentColor.ROSE -> if (isDark) roseDarkScheme else roseLightScheme
    }
}

/**
 * PDF Reader Pro Theme - Supports dark, light, and system theme modes with selectable accent colors.
 */
@Composable
fun PdfReaderProTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.DEFAULT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
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
        else -> getColorScheme(accentColor, isDarkTheme)
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
