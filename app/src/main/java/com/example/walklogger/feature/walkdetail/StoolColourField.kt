package com.example.walklogger.feature.walkdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.component.StoolSwatch
import com.example.walklogger.feature.logwalk.component.labelRes

private val SwatchSize = 20.dp

/** The stored stool [colour] as a small swatch beside its name, laid out like a DetailField. */
@Composable
internal fun StoolColourField(colour: StoolColour, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(text = stringResource(R.string.stool_colour), style = MaterialTheme.typography.bodyMedium, color = InkMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            StoolSwatch(colour = colour, size = SwatchSize)
            Text(text = stringResource(colour.labelRes), style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StoolColourFieldPreview() {
    WalkLoggerTheme {
        StoolColourField(colour = StoolColour.RED_STREAKED, modifier = Modifier.padding(AppSpacing.lg))
    }
}
