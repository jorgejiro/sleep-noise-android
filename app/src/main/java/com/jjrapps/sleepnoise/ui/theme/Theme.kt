package com.jjrapps.sleepnoise.ui.theme

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark only, and warm. There is no light scheme and no dynamic colour: this app is
 * used in bed with the lights off, so the theme is one deliberate world rather than
 * a pair. See ADR 002.
 */
private val SleepNoiseColorScheme = darkColorScheme(
    primary = SleepNoiseColors.Accent,
    onPrimary = SleepNoiseColors.OnAccent,
    primaryContainer = SleepNoiseColors.SurfaceSelected,
    onPrimaryContainer = SleepNoiseColors.OnBackground,
    secondary = SleepNoiseColors.Accent,
    onSecondary = SleepNoiseColors.OnAccent,
    background = SleepNoiseColors.Background,
    onBackground = SleepNoiseColors.OnBackground,
    surface = SleepNoiseColors.Background,
    onSurface = SleepNoiseColors.OnBackground,
    surfaceVariant = SleepNoiseColors.Surface,
    onSurfaceVariant = SleepNoiseColors.OnBackgroundVariant,
    surfaceContainer = SleepNoiseColors.Surface,
    surfaceContainerHigh = SleepNoiseColors.SurfaceRaised,
    surfaceContainerHighest = SleepNoiseColors.SurfaceSelected,
    outline = SleepNoiseColors.Outline,
    outlineVariant = SleepNoiseColors.Outline
)

@Composable
fun SleepNoiseTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = SleepNoiseColorScheme,
        shapes = SleepNoiseShapes,
        typography = SleepNoiseTypography,
        content = content
    )
}
