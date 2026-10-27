package com.example.walklogger.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

// Defaults to the light palette so previews without WalkLoggerTheme still render.
private val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

// Fixed brand palette (no dynamic colour) so the app looks like the mockups on every device.
private fun AppColors.toLightColorScheme(): ColorScheme = lightColorScheme(
    primary = action,
    onPrimary = onHeader,
    primaryContainer = header,
    onPrimaryContainer = onHeader,
    secondary = headerRaised,
    onSecondary = onHeader,
    background = background,
    onBackground = content,
    surface = background,
    onSurface = content,
    surfaceVariant = card,
    onSurfaceVariant = contentMuted,
    surfaceContainer = chip,
    outline = outline,
    outlineVariant = outline,
)

@Composable
fun WalkLoggerTheme(content: @Composable () -> Unit) {
    val colors = LightAppColors
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toLightColorScheme(),
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
