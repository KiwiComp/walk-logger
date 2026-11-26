package com.example.walklogger.core.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Control sizes shared by more than one component. Per-component sizes stay private to their file. */
object AppSize {
    /** Height of full-width actions (AppButton, DashedButton). */
    val buttonHeight: Dp = 56.dp

    /** Height of inline choice controls (SelectableChip, SegmentedToggle). */
    val chipHeight: Dp = 44.dp

    /** Height of rating faces on the walk cards */
    val singleRatingHeight: Dp = 28.dp

    /** Height of the route map (WalkRouteMap) on Finish walk and Walk detail. */
    val mapHeight: Dp = 200.dp

    /** Keeps the header title on one line beside DogBadge and the settings icon on 360dp screens. */
    val dogBadgeMaxWidth: Dp = 120.dp
}
