package com.watchlist.anihub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalTitleLanguage = compositionLocalOf { TitleLanguage.ROMAJI }
val LocalScoreFormat = compositionLocalOf { ScoreFormat.POINT_10 }
val LocalShowAiringCountdown = compositionLocalOf { true }

private fun getLightColorScheme(palette: ColorPalette) = when (palette) {
    ColorPalette.DYNAMIC -> lightColorScheme(primary = Primary, secondary = Secondary, tertiary = Tertiary)
    ColorPalette.BROWN -> lightColorScheme(primary = BrownPrimary, secondary = BrownSecondary, tertiary = BrownTertiary)
    ColorPalette.DEEP_BROWN -> lightColorScheme(primary = DeepBrownPrimary, secondary = DeepBrownSecondary, tertiary = DeepBrownTertiary)
    ColorPalette.PURPLE -> lightColorScheme(primary = PurplePrimary, secondary = PurpleSecondary, tertiary = PurpleTertiary)
    ColorPalette.DEEP_PURPLE -> lightColorScheme(primary = DeepPurplePrimary, secondary = DeepPurpleSecondary, tertiary = DeepPurpleTertiary)
}

private fun getDarkColorScheme(palette: ColorPalette, isAmoled: Boolean) = when (palette) {
    ColorPalette.DYNAMIC -> darkColorScheme(primary = Primary, secondary = Secondary, tertiary = Tertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey)
    ColorPalette.BROWN -> darkColorScheme(primary = BrownPrimary, secondary = BrownSecondary, tertiary = BrownTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey)
    ColorPalette.DEEP_BROWN -> darkColorScheme(primary = DeepBrownPrimary, secondary = DeepBrownSecondary, tertiary = DeepBrownTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey)
    ColorPalette.PURPLE -> darkColorScheme(primary = PurplePrimary, secondary = PurpleSecondary, tertiary = PurpleTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey)
    ColorPalette.DEEP_PURPLE -> darkColorScheme(primary = DeepPurplePrimary, secondary = DeepPurpleSecondary, tertiary = DeepPurpleTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey)
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
    val colorScheme = when {
        colorPalette == ColorPalette.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDarkColorScheme(colorPalette, themeMode == ThemeMode.AMOLED)
        else -> getLightColorScheme(colorPalette)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
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
