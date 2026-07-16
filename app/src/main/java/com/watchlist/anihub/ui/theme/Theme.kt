package com.watchlist.anihub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalTitleLanguage = compositionLocalOf { TitleLanguage.ROMAJI }
val LocalScoreFormat = compositionLocalOf { ScoreFormat.POINT_10 }
val LocalShowAiringCountdown = compositionLocalOf { true }

private fun getLightColorScheme(palette: ColorPalette): ColorScheme {
    val (primary, secondary, surface) = when (palette) {
        ColorPalette.DYNAMIC -> arrayOf(AniListBlue, AniListBlue.copy(alpha = 0.8f), Color(0xFFFDFBFF))
        ColorPalette.SAKURA -> arrayOf(SakuraAccent, SakuraSecondary, SakuraSurface)
        ColorPalette.OCEAN -> arrayOf(OceanAccent, OceanSecondary, OceanSurface)
        ColorPalette.FOREST -> arrayOf(ForestAccent, ForestSecondary, ForestSurface)
        ColorPalette.LAVENDER -> arrayOf(LavenderAccent, LavenderSecondary, LavenderSurface)
        ColorPalette.MIDNIGHT -> arrayOf(MidnightAccent, MidnightSecondary, Color(0xFFFDFBFF)) // Use standard light bg for Midnight in light mode
        ColorPalette.SUNSET -> arrayOf(SunsetAccent, SunsetSecondary, SunsetSurface)
        ColorPalette.ARCTIC -> arrayOf(ArcticAccent, ArcticSecondary, ArcticSurface)
        ColorPalette.MATCHA -> arrayOf(MatchaAccent, MatchaSecondary, MatchaSurface)
        ColorPalette.CYBER -> arrayOf(CyberAccent, CyberSecondary, Color(0xFFFDFBFF)) // Use standard light bg for Cyber in light mode
        ColorPalette.AMBER -> arrayOf(AmberAccent, AmberSecondary, AmberSurface)
    }

    return lightColorScheme(
        primary = primary,
        secondary = secondary,
        background = surface,
        surface = surface,
        primaryContainer = primary.copy(alpha = 0.1f),
        onPrimaryContainer = primary,
        surfaceVariant = surface.copy(alpha = 0.9f),
        surfaceContainer = surface
    )
}

private fun getDarkColorScheme(palette: ColorPalette, isAmoled: Boolean): ColorScheme {
    val (primary, secondary, surface) = when (palette) {
        ColorPalette.DYNAMIC -> arrayOf(AniListBlue, AniListBlue.copy(alpha = 0.8f), DarkGrey)
        ColorPalette.SAKURA -> arrayOf(SakuraAccent, SakuraSecondary, DarkGrey)
        ColorPalette.OCEAN -> arrayOf(OceanAccent, OceanSecondary, DarkGrey)
        ColorPalette.FOREST -> arrayOf(ForestAccent, ForestSecondary, DarkGrey)
        ColorPalette.LAVENDER -> arrayOf(LavenderAccent, LavenderSecondary, DarkGrey)
        ColorPalette.MIDNIGHT -> arrayOf(MidnightAccent, MidnightSecondary, MidnightSurface)
        ColorPalette.SUNSET -> arrayOf(SunsetAccent, SunsetSecondary, DarkGrey)
        ColorPalette.ARCTIC -> arrayOf(ArcticAccent, ArcticSecondary, DarkGrey)
        ColorPalette.MATCHA -> arrayOf(MatchaAccent, MatchaSecondary, DarkGrey)
        ColorPalette.CYBER -> arrayOf(CyberAccent, CyberSecondary, CyberSurface)
        ColorPalette.AMBER -> arrayOf(AmberAccent, AmberSecondary, DarkGrey)
    }

    val background = if (isAmoled) DeepBlack else surface
    val actualSurface = if (isAmoled) DeepBlack else surface

    return darkColorScheme(
        primary = primary,
        secondary = secondary,
        background = background,
        surface = actualSurface,
        primaryContainer = primary.copy(alpha = 0.2f),
        onPrimaryContainer = primary,
        surfaceContainer = if (isAmoled) Color(0xFF0A0A0A) else surface.copy(alpha = 1.2f)
    )
}

@Composable
fun AnihubTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    colorPalette: ColorPalette = ColorPalette.DYNAMIC,
    titleLanguage: TitleLanguage = TitleLanguage.ROMAJI,
    scoreFormat: ScoreFormat = ScoreFormat.POINT_10,
    showAiringCountdown: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AMOLED -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    var colorScheme = when {
        colorPalette == ColorPalette.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDarkColorScheme(colorPalette, themeMode == ThemeMode.AMOLED)
        else -> getLightColorScheme(colorPalette)
    }

    // AMOLED optimizations
    if (themeMode == ThemeMode.AMOLED) {
        colorScheme = colorScheme.copy(
            background = DeepBlack,
            surface = DeepBlack,
            surfaceContainer = Color(0xFF0A0A0A),
            surfaceContainerHigh = Color(0xFF141414),
            surfaceVariant = Color(0xFF1A1A1A)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalTitleLanguage provides titleLanguage,
        LocalScoreFormat provides scoreFormat,
        LocalShowAiringCountdown provides showAiringCountdown
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
