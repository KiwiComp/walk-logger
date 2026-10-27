package com.example.walklogger.feature.logwalk.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.StoolBlack
import com.example.walklogger.core.ui.theme.StoolBrightYellow
import com.example.walklogger.core.ui.theme.StoolDarkBrown
import com.example.walklogger.core.ui.theme.StoolGreen
import com.example.walklogger.core.ui.theme.StoolGrey
import com.example.walklogger.core.ui.theme.StoolLightBrown
import com.example.walklogger.core.ui.theme.StoolMediumBrown
import com.example.walklogger.core.ui.theme.StoolYellowyBrown
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** A round sample of [colour]; the patterned ones show their texture drawable over the base colour. */
@Composable
internal fun StoolSwatch(colour: StoolColour, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colour.baseColour)
            .border(1.dp, AppTheme.colors.outline, CircleShape),
    ) {
        colour.textureRes?.let { texture ->
            Image(
                painter = painterResource(texture),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

private val StoolColour.baseColour
    get() = when (this) {
        StoolColour.LIGHT_BROWN -> StoolLightBrown
        StoolColour.MEDIUM_BROWN, StoolColour.RED_STREAKED, StoolColour.WHITE_SPECKED -> StoolMediumBrown
        StoolColour.DARK_BROWN -> StoolDarkBrown
        StoolColour.BLACK -> StoolBlack
        StoolColour.YELLOWY_BROWN -> StoolYellowyBrown
        StoolColour.BRIGHT_YELLOW -> StoolBrightYellow
        StoolColour.GREY -> StoolGrey
        StoolColour.GREEN -> StoolGreen
    }

private val StoolColour.textureRes: Int?
    @DrawableRes get() = when (this) {
        StoolColour.RED_STREAKED -> R.drawable.stool_swatch_red_streaked
        StoolColour.WHITE_SPECKED -> R.drawable.stool_swatch_white_specked
        else -> null
    }

@Preview(showBackground = true)
@Composable
private fun StoolSwatchPreview() {
    WalkLoggerTheme {
        FlowRow(
            modifier = Modifier.padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            StoolColour.entries.forEach { StoolSwatch(colour = it, size = AppSize.chipHeight) }
        }
    }
}
