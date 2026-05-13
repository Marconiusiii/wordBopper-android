package com.marconius.wordbopper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WordBopperColorScheme = darkColorScheme(
    background = WbBackground,
    surface = WbSurface,
    surfaceVariant = WbPanel,
    primary = WbAccent5,
    onPrimary = WbBackground,
    secondary = WbAccent4,
    onSecondary = WbBackground,
    tertiary = WbAccent1,
    onTertiary = WbBackground,
    onBackground = WbText,
    onSurface = WbText,
    onSurfaceVariant = WbMuted,
    error = WbAccent2,
    outline = WbMuted,
)

@Composable
fun WordBopperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WordBopperColorScheme,
        typography = WordBopperTypography,
        content = content
    )
}
