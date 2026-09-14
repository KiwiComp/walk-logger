package com.example.walklogger.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(AppRadius.xs),
    small = RoundedCornerShape(AppRadius.sm),
    medium = RoundedCornerShape(AppRadius.md),
    large = RoundedCornerShape(AppRadius.lg),
    extraLarge = RoundedCornerShape(AppRadius.xl),
)

/** Material's scale has no zero-radius slot, so the square shape lives beside it. */
val SquareShape = RoundedCornerShape(AppRadius.none)
