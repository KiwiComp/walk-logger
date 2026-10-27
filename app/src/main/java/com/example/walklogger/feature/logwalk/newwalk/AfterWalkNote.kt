package com.example.walklogger.feature.logwalk.newwalk

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Dashed box explaining what is asked for after the walk instead of now. */
@Composable
internal fun AfterWalkNote(modifier: Modifier = Modifier) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
    val outlineColor = AppTheme.colors.outline
    Text(
        text = stringResource(R.string.new_walk_after_note),
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colors.contentMuted,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind { drawRect(color = outlineColor, style = Stroke(width = 3f, pathEffect = dash)) }
            .padding(AppSpacing.xl),
    )
}

@Preview(showBackground = true)
@Composable
private fun AfterWalkNotePreview() {
    WalkLoggerTheme {
        AfterWalkNote(modifier = Modifier.padding(AppSpacing.lg))
    }
}
