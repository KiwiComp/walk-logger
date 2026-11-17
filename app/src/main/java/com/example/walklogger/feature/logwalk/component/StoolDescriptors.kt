package com.example.walklogger.feature.logwalk.component

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
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Amount, texture and colour rows describing a passed [stool]. */
@Composable
internal fun StoolDescriptors(
    stool: Stool?,
    onQuantitySelect: (StoolQuantity) -> Unit,
    onTextureSelect: (StoolTexture) -> Unit,
    onColourSelect: (StoolColour?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        DescriptorRow(label = stringResource(R.string.stool_quantity)) {
            StepSlider(
                options = StoolQuantity.entries,
                selected = stool?.quantity,
                onSelect = onQuantitySelect,
                label = { stringResource(it.labelRes) },
            )
        }
        DescriptorRow(label = stringResource(R.string.stool_texture)) {
            StepSlider(
                options = StoolTexture.entries,
                selected = stool?.texture,
                onSelect = onTextureSelect,
                label = { stringResource(it.labelRes) },
            )
        }
        DescriptorRow(label = stringResource(R.string.stool_colour)) {
            StoolColourPicker(selected = stool?.colour, onSelect = onColourSelect)
        }
    }
}

@Composable
private fun DescriptorRow(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.contentMuted,
        )
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun StoolDescriptorsPreview() {
    WalkLoggerTheme {
        StoolDescriptors(
            stool = Stool(
                passed = true,
                quantity = StoolQuantity.NORMAL,
                colour = StoolColour.MEDIUM_BROWN,
            ),
            onQuantitySelect = {},
            onTextureSelect = {},
            onColourSelect = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
