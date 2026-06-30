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

private fun getLightColorScheme(palette: ColorPalette) = when (palette) {
    ColorPalette.DYNAMIC -> lightColorScheme(primary = Primary, secondary = Secondary, tertiary = Tertiary, primaryContainer = Primary.copy(alpha = 0.1f), onPrimaryContainer = Primary)
    ColorPalette.BROWN -> lightColorScheme(primary = BrownPrimary, secondary = BrownSecondary, tertiary = BrownTertiary, primaryContainer = BrownPrimary.copy(alpha = 0.1f), onPrimaryContainer = BrownPrimary)
    ColorPalette.DEEP_BROWN -> lightColorScheme(primary = DeepBrownPrimary, secondary = DeepBrownSecondary, tertiary = DeepBrownTertiary, primaryContainer = DeepBrownPrimary.copy(alpha = 0.1f), onPrimaryContainer = DeepBrownPrimary)
    ColorPalette.PURPLE -> lightColorScheme(primary = PurplePrimary, secondary = PurpleSecondary, tertiary = PurpleTertiary, primaryContainer = PurplePrimary.copy(alpha = 0.1f), onPrimaryContainer = PurplePrimary)
    ColorPalette.DEEP_PURPLE -> lightColorScheme(primary = DeepPurplePrimary, secondary = DeepPurpleSecondary, tertiary = DeepPurpleTertiary, primaryContainer = DeepPurplePrimary.copy(alpha = 0.1f), onPrimaryContainer = DeepPurplePrimary)
    ColorPalette.OCEAN -> lightColorScheme(primary = OceanPrimary, secondary = OceanSecondary, tertiary = OceanTertiary, primaryContainer = OceanPrimary.copy(alpha = 0.1f), onPrimaryContainer = OceanPrimary)
    ColorPalette.FOREST -> lightColorScheme(primary = ForestPrimary, secondary = ForestSecondary, tertiary = ForestTertiary, primaryContainer = ForestPrimary.copy(alpha = 0.1f), onPrimaryContainer = ForestPrimary)
    ColorPalette.CHERRY -> lightColorScheme(primary = CherryPrimary, secondary = CherrySecondary, tertiary = CherryTertiary, primaryContainer = CherryPrimary.copy(alpha = 0.1f), onPrimaryContainer = CherryPrimary)
    ColorPalette.SUNSET -> lightColorScheme(primary = SunsetPrimary, secondary = SunsetSecondary, tertiary = SunsetTertiary, primaryContainer = SunsetPrimary.copy(alpha = 0.1f), onPrimaryContainer = SunsetPrimary)
    ColorPalette.LAVENDER -> lightColorScheme(primary = LavenderPrimary, secondary = LavenderSecondary, tertiary = LavenderTertiary, primaryContainer = LavenderPrimary.copy(alpha = 0.1f), onPrimaryContainer = LavenderPrimary)
    ColorPalette.MINT -> lightColorScheme(primary = MintPrimary, secondary = MintSecondary, tertiary = MintTertiary, primaryContainer = MintPrimary.copy(alpha = 0.1f), onPrimaryContainer = MintPrimary)
    ColorPalette.GOLD -> lightColorScheme(primary = GoldPrimary, secondary = GoldSecondary, tertiary = GoldTertiary, primaryContainer = GoldPrimary.copy(alpha = 0.1f), onPrimaryContainer = GoldPrimary)
}

private fun getDarkColorScheme(palette: ColorPalette, isAmoled: Boolean) = when (palette) {
    ColorPalette.DYNAMIC -> darkColorScheme(primary = Primary, secondary = Secondary, tertiary = Tertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = Primary.copy(alpha = 0.2f), onPrimaryContainer = Primary)
    ColorPalette.BROWN -> darkColorScheme(primary = BrownPrimary, secondary = BrownSecondary, tertiary = BrownTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = BrownPrimary.copy(alpha = 0.2f), onPrimaryContainer = BrownPrimary)
    ColorPalette.DEEP_BROWN -> darkColorScheme(primary = DeepBrownPrimary, secondary = DeepBrownSecondary, tertiary = DeepBrownTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = DeepBrownPrimary.copy(alpha = 0.2f), onPrimaryContainer = DeepBrownPrimary)
    ColorPalette.PURPLE -> darkColorScheme(primary = PurplePrimary, secondary = PurpleSecondary, tertiary = PurpleTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = PurplePrimary.copy(alpha = 0.2f), onPrimaryContainer = PurplePrimary)
    ColorPalette.DEEP_PURPLE -> darkColorScheme(primary = DeepPurplePrimary, secondary = DeepPurpleSecondary, tertiary = DeepPurpleTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = DeepPurplePrimary.copy(alpha = 0.2f), onPrimaryContainer = DeepPurplePrimary)
    ColorPalette.OCEAN -> darkColorScheme(primary = OceanPrimary, secondary = OceanSecondary, tertiary = OceanTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = OceanPrimary.copy(alpha = 0.2f), onPrimaryContainer = OceanPrimary)
    ColorPalette.FOREST -> darkColorScheme(primary = ForestPrimary, secondary = ForestSecondary, tertiary = ForestTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = ForestPrimary.copy(alpha = 0.2f), onPrimaryContainer = ForestPrimary)
    ColorPalette.CHERRY -> darkColorScheme(primary = CherryPrimary, secondary = CherrySecondary, tertiary = CherryTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = CherryPrimary.copy(alpha = 0.2f), onPrimaryContainer = CherryPrimary)
    ColorPalette.SUNSET -> darkColorScheme(primary = SunsetPrimary, secondary = SunsetSecondary, tertiary = SunsetTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = SunsetPrimary.copy(alpha = 0.2f), onPrimaryContainer = SunsetPrimary)
    ColorPalette.LAVENDER -> darkColorScheme(primary = LavenderPrimary, secondary = LavenderSecondary, tertiary = LavenderTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = LavenderPrimary.copy(alpha = 0.2f), onPrimaryContainer = LavenderPrimary)
    ColorPalette.MINT -> darkColorScheme(primary = MintPrimary, secondary = MintSecondary, tertiary = MintTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = MintPrimary.copy(alpha = 0.2f), onPrimaryContainer = MintPrimary)
    ColorPalette.GOLD -> darkColorScheme(primary = GoldPrimary, secondary = GoldSecondary, tertiary = GoldTertiary, background = if (isAmoled) DeepBlack else DarkGrey, surface = if (isAmoled) DeepBlack else DarkGrey, primaryContainer = GoldPrimary.copy(alpha = 0.2f), onPrimaryContainer = GoldPrimary)
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

    // Force AMOLED black background if selected
    if (themeMode == ThemeMode.AMOLED) {
        colorScheme = colorScheme.copy(
            background = DeepBlack,
            surface = DeepBlack,
            surfaceVariant = Color(0xFF121212),
            onBackground = Color.White,
            onSurface = Color.White,
            surfaceContainer = Color(0xFF0A0A0A)
        )
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
