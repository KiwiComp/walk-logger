package com.example.walklogger.feature.logwalk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Small-caps [label] (with an optional [hint] beside it) above a block of form controls. */
@Composable
internal fun FormSection(
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = label, color = InkMuted)
            if (hint != null) {
                Text(text = hint, style = MaterialTheme.typography.bodySmall, color = InkMuted)
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun FormSectionPreview() {
    WalkLoggerTheme {
        FormSection(label = "Rating", hint = "1 worst · 7 best", modifier = Modifier.padding(AppSpacing.lg)) {
            Text("content")
        }
    }
}
