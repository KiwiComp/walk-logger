package com.example.walklogger.feature.logwalk.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/**
 * Small-caps [label] (with an optional [hint] beside it) above a block of form controls.
 * Pass [onToggle] to make the section collapsible; [expanded] then decides whether [content] is shown.
 * [actionLabel] with [onAction] adds a small text action ("Clear") at the end of the header row.
 */
@Composable
internal fun FormSection(
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    expanded: Boolean = true,
    onToggle: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onToggle != null) Modifier.clickable(role = Role.Button, onClick = onToggle) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = label, color = InkMuted, style = MaterialTheme.typography.labelLarge)
            if (hint != null) {
                Text(text = hint, style = MaterialTheme.typography.bodySmall, color = InkMuted)
            }
            Spacer(Modifier.weight(1f))
            if (actionLabel != null && onAction != null) {
                SectionLabel(
                    text = actionLabel,
                    color = Steel,
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onAction)
                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                )
            }
            if (onToggle != null) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = InkMuted,
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(AppSpacing.sm))
                content()
            }
        }
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

@Preview(showBackground = true)
@Composable
private fun FormSectionCollapsedPreview() {
    WalkLoggerTheme {
        FormSection(
            label = "Weather",
            expanded = false,
            onToggle = {},
            actionLabel = "Clear",
            onAction = {},
            modifier = Modifier.padding(AppSpacing.lg),
        ) {
            Text("content")
        }
    }
}
