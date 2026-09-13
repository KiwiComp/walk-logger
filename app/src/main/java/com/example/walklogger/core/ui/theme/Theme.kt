package com.example.walklogger.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Fixed brand palette (no dynamic colour) so the app looks like the mockups on every device.
private val LightColorScheme = lightColorScheme(
    primary = Steel,
    onPrimary = OnNavy,
    primaryContainer = Navy,
    onPrimaryContainer = OnNavy,
    secondary = NavyLight,
    onSecondary = OnNavy,
    background = Canvas,
    onBackground = Ink,
    surface = Canvas,
    onSurface = Ink,
    surfaceVariant = CardSurface,
    onSurfaceVariant = InkMuted,
    surfaceContainer = ChipSurface,
    outline = Outline,
    outlineVariant = Outline,
)

@Composable
fun WalkLoggerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
