package com.example.walklogger.core.ui.theme

import androidx.compose.ui.graphics.Color

// Palette taken from the design mockups. Named by role, not by hue, so screens
// reference e.g. `Navy` for the header and never hard-code hex values.
val Navy = Color(0xFF1F2D3D)
val NavyLight = Color(0xFF34435A)
val Steel = Color(0xFF5E7A9C)
val SteelLight = Color(0xFF8FA7C2)

val Canvas = Color(0xFFF2F2F0)
val CardSurface = Color(0xFFF8F8F6)
val ChipSurface = Color(0xFFE8EAED)
val Outline = Color(0xFFD5D8DC)
val OutlineOnNavy = Color(0x66FFFFFF)

val Ink = Color(0xFF1F2D3D)
val InkMuted = Color(0xFF7A8189)
val OnNavy = Color(0xFFFFFFFF)
val OnNavyMuted = Color(0xFFB9C2CE)

/** The one warm colour, reserved for marking a selection on top of the blues (calendar day). */
val Accent = Color(0xFFD9A441)

/** One shade per rating 1..7, lightest first; the top four are the palette blues. Index with `rating - 1`. */
val RatingTints: List<Color> = listOf(
    Color(0xFFDCE4EE),
    Color(0xFFC4D2E2),
    Color(0xFFA9BDD3),
    SteelLight,
    Steel,
    NavyLight,
    Navy,
)

// Stool swatches: the closest everyday match to what a handler sees, not part of the UI palette.
// The streaked and specked swatches are drawables (res/drawable/stool_swatch_*) over StoolMediumBrown.
val StoolLightBrown = Color(0xFFC8A27A)
val StoolMediumBrown = Color(0xFF8B5A2B)
val StoolDarkBrown = Color(0xFF4E3221)
val StoolBlack = Color(0xFF1E1A18)
val StoolYellowyBrown = Color(0xFFB8893A)
val StoolBrightYellow = Color(0xFFE8C547)
val StoolGrey = Color(0xFFA39E98)
val StoolGreen = Color(0xFF6B7F3A)
