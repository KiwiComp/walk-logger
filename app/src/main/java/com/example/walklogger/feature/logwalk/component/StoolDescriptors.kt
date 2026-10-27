package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.ui.component.SegmentedToggle
import com.example.walklogger.core.ui.component.SegmentedToggleSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Amount, texture and colour rows describing a passed [stool]. */
@Composable
internal fun StoolDescriptors(
    stool: Stool?,
    onQuantitySelected: (StoolQuantity) -> Unit,
    onTextureSelected: (StoolTexture) -> Unit,
    onColourSelected: (StoolColour?) -> Unit,
    modifier: Modifier = Modifier,
    style: FormStyle = FormStyle.Default,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        DescriptorRow(label = stringResource(R.string.stool_quantity), style = style) {
            SegmentedToggle(
                options = StoolQuantity.entries,
                selected = stool?.quantity,
                onSelect = onQuantitySelected,
                label = { stringResource(it.labelRes) },
                size = SegmentedToggleSize.Compact,
            )
        }
        DescriptorRow(label = stringResource(R.string.stool_texture), style = style) {
            SegmentedToggle(
                options = StoolTexture.entries,
                selected = stool?.texture,
                onSelect = onTextureSelected,
                label = { stringResource(it.labelRes) },
                size = SegmentedToggleSize.Compact,
            )
        }
        DescriptorRow(label = stringResource(R.string.stool_colour), style = style) {
            StoolColourPicker(selected = stool?.colour, onSelect = onColourSelected, style = style)
        }
    }
}

@Composable
private fun DescriptorRow(label: String, style: FormStyle, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = style.muted)
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun StoolDescriptorsPreview() {
    WalkLoggerTheme {
        StoolDescriptors(
            stool = Stool(passed = true, quantity = StoolQuantity.NORMAL, colour = StoolColour.MEDIUM_BROWN),
            onQuantitySelected = {},
            onTextureSelected = {},
            onColourSelected = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview
@Composable
private fun StoolDescriptorsInversePreview() {
    WalkLoggerTheme {
        StoolDescriptors(
            stool = Stool(passed = true, texture = StoolTexture.SOFT, colour = StoolColour.WHITE_SPECKED),
            onQuantitySelected = {},
            onTextureSelected = {},
            onColourSelected = {},
            style = FormStyle.Inverse,
            modifier = Modifier.background(AppTheme.colors.header).padding(AppSpacing.lg),
        )
    }
}
