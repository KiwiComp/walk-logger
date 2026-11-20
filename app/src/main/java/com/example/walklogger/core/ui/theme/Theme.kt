package com.example.walklogger.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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
private fun AppColors.toColorScheme(darkTheme: Boolean): ColorScheme =
    (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
        primary = action,
        onPrimary = onAction,
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
fun WalkLoggerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkAppColors else LightAppColors
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(darkTheme),
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
