package com.example.walklogger.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Palette from the 2026-11 redesign (warm neutrals + forest green). Private so screens go through
// the semantic roles in [AppColors]; a dark theme or new palette then only changes this file.
private val Ink = Color(0xFF17212B) // primary text, dark hero card, selected day
private val InkRaised = Color(0xFF26323F) // tile on the dark hero card
private val InkSecondary = Color(0xFF3E4752) // body text, sub-labels
private val InkMuted = Color(0xFF5B6470) // captions, meta text (passes 4.5:1 on Canvas)
private val InkPlaceholder = Color(0xFF6B7480) // placeholders, "(No description)", future days
private val OnInk = Color(0xFFFFFFFF)
private val OnInkMuted = Color(0xFFB9C2CC)

private val Canvas = Color(0xFFF4F3EF) // screen background
private val Surface = Color(0xFFFFFFFF) // cards, inputs, bottom bars
private val SurfaceSubtle = Color(0xFFF9F8F5) // walk cards inside a white sheet, text areas
private val Track = Color(0xFFECEAE4) // segmented-control track, "Edit" pill
private val Outline = Color(0xFFE6E3DC) // card borders, dividers
private val OutlineStrong = Color(0xFFD6D3CA) // input / chip / button borders
private val Rail = Color(0xFFE2DFD8) // timeline line, empty slider track
private val Dashed = Color(0xFFA9A59B) // dashed "Other…" chips, chevrons

private val Green = Color(0xFF2E6A4F) // primary action, selected chip, today ring
private val GreenSoft = Color(0xFFE3EEE8) // selected-but-soft backgrounds, active nav pill
private val GreenOnSoft = Color(0xFF1F4D38) // text/icons on GreenSoft
private val GreenFocus = Color(0xFFCFE3D8) // input focus ring

private val WarnSoft = Color(0xFFFBEBD3) // selected health concern
private val WarnOnSoft = Color(0xFF6B3F06)
private val WarnOutline = Color(0xFFD9963A)
private val Danger = Color(0xFFB3261E) // "Delete walk" / "Delete dog"

private val DisabledSurface = Color(0xFFEEECE7)
private val DisabledContent = Color(0xFF9A9EA4)

/** Colour roles the UI reads through [AppTheme.colors]; never reference palette hues directly. */
@Immutable
class AppColors(
    val background: Color,
    val card: Color,
    val cardSubtle: Color,
    val chip: Color,
    val outline: Color,
    val outlineStrong: Color,
    val rail: Color,
    val dashed: Color,
    val content: Color,
    val contentSecondary: Color,
    val contentMuted: Color,
    val placeholder: Color,
    val action: Color,
    val onAction: Color,
    val actionLight: Color,
    val onActionLight: Color,
    val focusRing: Color,
    /** Dark hero card on "Logged walk" (was the navy top bar). */
    val header: Color,
    val headerRaised: Color,
    val onHeader: Color,
    val onHeaderMuted: Color,
    /** Selected day in week strip and calendar. Today is marked with [action] as a ring. */
    val selection: Color,
    val onSelection: Color,
    val warningLight: Color,
    val onWarningLight: Color,
    val warningOutline: Color,
    val danger: Color,
    val disabled: Color,
    val onDisabled: Color,
    /** One colour per rating 1..7, red (worst) to green (best). Index with `rating - 1`. */
    val ratingTints: List<Color>,
    /** Eyes and mouth on the rating faces. */
    val ratingFaceInk: Color,
)

internal val LightAppColors = AppColors(
    background = Canvas,
    card = Surface,
    cardSubtle = SurfaceSubtle,
    chip = Track,
    outline = Outline,
    outlineStrong = OutlineStrong,
    rail = Rail,
    dashed = Dashed,
    content = Ink,
    contentSecondary = InkSecondary,
    contentMuted = InkMuted,
    placeholder = InkPlaceholder,
    action = Green,
    onAction = OnInk,
    actionLight = GreenSoft,
    onActionLight = GreenOnSoft,
    focusRing = GreenFocus,
    header = Ink,
    headerRaised = InkRaised,
    onHeader = OnInk,
    onHeaderMuted = OnInkMuted,
    selection = Ink,
    onSelection = OnInk,
    warningLight = WarnSoft,
    onWarningLight = WarnOnSoft,
    warningOutline = WarnOutline,
    danger = Danger,
    disabled = DisabledSurface,
    onDisabled = DisabledContent,
    ratingTints = listOf(
        Color(0xFFE0533D), // 1 very angry
        Color(0xFFEC7F3F), // 2 angry
        Color(0xFFF2A93E), // 3 sad
        Color(0xFFF0CB45), // 4 neutral
        Color(0xFFC5D352), // 5 slight smile
        Color(0xFF8CC85A), // 6 happy
        Color(0xFF4CAF68), // 7 very happy
    ),
    ratingFaceInk = Color(0xFF3B2A12),
)

// Stool swatches: unchanged. The redesign uses the same hues.
// The streaked and specked swatches are drawables (res/drawable/stool_swatch_*) over StoolMediumBrown.
val StoolLightBrown = Color(0xFFC8A27A)
val StoolMediumBrown = Color(0xFF8B5A2B)
val StoolDarkBrown = Color(0xFF4E3221)
val StoolBlack = Color(0xFF1E1A18)
val StoolYellowyBrown = Color(0xFFB8893A)
val StoolBrightYellow = Color(0xFFE8C547)
val StoolGrey = Color(0xFFA39E98)
val StoolGreen = Color(0xFF6B7F3A)
