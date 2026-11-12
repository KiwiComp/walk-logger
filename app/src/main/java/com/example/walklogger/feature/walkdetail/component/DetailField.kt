package com.example.walklogger.feature.walkdetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** A muted [label] over its stored [value]; the read-only counterpart of a form field. */
@Composable
internal fun DetailField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.contentMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.content,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailFieldPreview() {
    WalkLoggerTheme {
        DetailField(
            label = "Date",
            value = "Tue 15 Sep",
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
