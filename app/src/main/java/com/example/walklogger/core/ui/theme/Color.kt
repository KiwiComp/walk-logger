package com.example.walklogger.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Palette taken from the design mockups. Private so screens go through the semantic roles in
// [AppColors]; a dark theme or new palette then only changes this file.
private val Navy = Color(0xFF1F2D3D)
private val NavyLight = Color(0xFF34435A)
private val Steel = Color(0xFF5E7A9C)
private val SteelLight = Color(0xFF8FA7C2)

private val Canvas = Color(0xFFF2F2F0)
private val CardSurface = Color(0xFFF8F8F6)
private val ChipSurface = Color(0xFFE8EAED)
private val Outline = Color(0xFFD5D8DC)
private val OutlineOnNavy = Color(0x66FFFFFF)

private val Ink = Color(0xFF1F2D3D)
private val InkMuted = Color(0xFF7A8189)
private val OnNavy = Color(0xFFFFFFFF)
private val OnNavyMuted = Color(0xFFB9C2CE)

private val Accent = Color(0xFFD9A441)

/** Colour roles the UI reads through [AppTheme.colors]; never reference palette hues directly. */
@Immutable
class AppColors(
    val background: Color,
    val card: Color,
    val chip: Color,
    val outline: Color,
    val content: Color,
    val contentMuted: Color,
    val action: Color,
    val actionLight: Color,
    val header: Color,
    val headerRaised: Color,
    val onHeader: Color,
    val onHeaderMuted: Color,
    val outlineOnHeader: Color,
    /** The one warm colour, reserved for marking a selection on top of the blues (calendar day). */
    val selection: Color,
    /** One shade per rating 1..7, lightest first. Index with `rating - 1`. */
    val ratingTints: List<Color>,
)

internal val LightAppColors = AppColors(
    background = Canvas,
    card = CardSurface,
    chip = ChipSurface,
    outline = Outline,
    content = Ink,
    contentMuted = InkMuted,
    action = Steel,
    actionLight = SteelLight,
    header = Navy,
    headerRaised = NavyLight,
    onHeader = OnNavy,
    onHeaderMuted = OnNavyMuted,
    outlineOnHeader = OutlineOnNavy,
    selection = Accent,
    // The top four are the palette blues.
    ratingTints = listOf(
        Color(0xFFDCE4EE),
        Color(0xFFC4D2E2),
        Color(0xFFA9BDD3),
        SteelLight,
        Steel,
        NavyLight,
        Navy,
    ),
)

// Stool swatches: the closest everyday match to what a handler sees, not part of the UI palette,
// so they stay the same in every theme.
// The streaked and specked swatches are drawables (res/drawable/stool_swatch_*) over StoolMediumBrown.
val StoolLightBrown = Color(0xFFC8A27A)
val StoolMediumBrown = Color(0xFF8B5A2B)
val StoolDarkBrown = Color(0xFF4E3221)
val StoolBlack = Color(0xFF1E1A18)
val StoolYellowyBrown = Color(0xFFB8893A)
val StoolBrightYellow = Color(0xFFE8C547)
val StoolGrey = Color(0xFFA39E98)
val StoolGreen = Color(0xFF6B7F3A)
