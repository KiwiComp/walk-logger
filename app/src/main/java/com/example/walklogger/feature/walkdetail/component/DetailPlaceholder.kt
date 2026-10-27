package com.example.walklogger.feature.walkdetail.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Muted stand-in for a section the walk has nothing stored for ("Not recorded", "Nothing noted"). */
@Composable
internal fun DetailPlaceholder(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colors.contentMuted,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun DetailPlaceholderPreview() {
    WalkLoggerTheme {
        DetailPlaceholder(text = "Not recorded", modifier = Modifier.padding(AppSpacing.lg))
    }
}
