package com.example.walklogger.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Palette from the 2026-11 redesign (warm neutrals + forest green). Private so screens go through
// the semantic roles in [AppColors]; a new palette then only changes this file.
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

private val DarkCanvas = Color(0xFF12171D)
private val DarkSurface = Color(0xFF1B222A)
private val DarkSurfaceSubtle = Color(0xFF222A33)
private val DarkTrack = Color(0xFF262E37)
private val DarkOutline = Color(0xFF2C353F)
private val DarkOutlineStrong = Color(0xFF3A444F)
private val DarkRail = Color(0xFF333C46)
private val DarkDashed = Color(0xFF6B7480)

private val DarkInk = Color(0xFFECEFF2)
private val DarkInkSecondary = Color(0xFFC3C9D0)
private val DarkInkMuted = Color(0xFF9AA3AD)
private val DarkInkPlaceholder = Color(0xFF858E99)

private val DarkGreen = Color(0xFF6CC497) // lighter green, so it reads on dark
private val DarkOnGreen = Color(0xFF0F2A1D) // dark text on the green button
private val DarkGreenSoft = Color(0xFF1E3A2D)
private val DarkGreenOnSoft = Color(0xFFA9DFC2)
private val DarkGreenFocus = Color(0xFF2F5A44)

private val DarkHero = Color(0xFF232D38)
private val DarkHeroRaised = Color(0xFF2F3A47)
private val DarkOnHeroMuted = Color(0xFFAEB8C3)

private val DarkWarnSoft = Color(0xFF3D2C14)
private val DarkWarnOnSoft = Color(0xFFF4C98A)
private val DarkWarnOutline = Color(0xFFB57A2A)
private val DarkDanger = Color(0xFFFF8A80)

private val DarkDisabledSurface = Color(0xFF222930)
private val DarkDisabledContent = Color(0xFF5C6570)

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

internal val DarkAppColors = AppColors(
    background = DarkCanvas,
    card = DarkSurface,
    cardSubtle = DarkSurfaceSubtle,
    chip = DarkTrack,
    outline = DarkOutline,
    outlineStrong = DarkOutlineStrong,
    rail = DarkRail,
    dashed = DarkDashed,
    content = DarkInk,
    contentSecondary = DarkInkSecondary,
    contentMuted = DarkInkMuted,
    placeholder = DarkInkPlaceholder,
    action = DarkGreen,
    onAction = DarkOnGreen,
    actionLight = DarkGreenSoft,
    onActionLight = DarkGreenOnSoft,
    focusRing = DarkGreenFocus,
    header = DarkHero,
    headerRaised = DarkHeroRaised,
    onHeader = OnInk,
    onHeaderMuted = DarkOnHeroMuted,
    // Light mode marks the selected day dark; dark mode flips it to light.
    selection = DarkInk,
    onSelection = DarkCanvas,
    warningLight = DarkWarnSoft,
    onWarningLight = DarkWarnOnSoft,
    warningOutline = DarkWarnOutline,
    danger = DarkDanger,
    disabled = DarkDisabledSurface,
    onDisabled = DarkDisabledContent,
    // Same faces in both themes, so the rating reads the same.
    ratingTints = LightAppColors.ratingTints,
    ratingFaceInk = LightAppColors.ratingFaceInk,
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
