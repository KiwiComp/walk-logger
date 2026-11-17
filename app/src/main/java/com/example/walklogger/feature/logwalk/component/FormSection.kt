package com.example.walklogger.feature.logwalk.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/**
 * Titled block of form controls, drawn as a card unless [card] is false (sections in a divided list).
 * [hint] is a muted line under the title. Pass [onToggle] to make the section collapsible; [expanded]
 * then decides whether [content] is shown. [chevronUp] lets a section that folds only part of its content
 * (stool) point the chevron on its own.
 * [actionLabel] with [onAction] adds a small text action ("Clear") at the end of the header row. Its slot is
 * kept whenever [onAction] is passed, so a null [actionLabel] hides the action without changing the row's height.
 * [leadingActionLabel] with [onLeadingAction] adds a second one before it, shown only while the label is set.
 */
@Composable
internal fun FormSection(
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    card: Boolean = true,
    expanded: Boolean = true,
    onToggle: (() -> Unit)? = null,
    chevronUp: Boolean = expanded,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    leadingActionLabel: String? = null,
    onLeadingAction: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.lg)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (card) {
                    Modifier
                        .clip(shape)
                        .background(AppTheme.colors.card)
                        .border(1.dp, AppTheme.colors.outline, shape)
                        .padding(AppSpacing.lg)
                } else {
                    Modifier
                },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onToggle != null) {
                        Modifier.clickable(role = Role.Button, onClick = onToggle)
                    } else {
                        Modifier
                    },
                ),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.content,
                )
                if (hint != null) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.contentMuted,
                    )
                }
            }
            if (leadingActionLabel != null && onLeadingAction != null) {
                ActionText(text = leadingActionLabel, onClick = onLeadingAction)
            }
            if (onAction != null) {
                ActionText(
                    text = actionLabel.orEmpty(),
                    onClick = onAction.takeIf {
                        actionLabel !=
                            null
                    },
                )
            }
            if (onToggle != null) {
                Icon(
                    imageVector = if (chevronUp) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = AppTheme.colors.contentMuted,
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(AppSpacing.md))
                content()
            }
        }
    }
}

@Composable
private fun ActionText(text: String, onClick: (() -> Unit)?) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = AppTheme.colors.action,
        modifier = Modifier
            .then(
                if (onClick !=
                    null
                ) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
    )
}

@Preview(showBackground = true)
@Composable
private fun FormSectionPreview() {
    WalkLoggerTheme {
        FormSection(
            label = "Rating",
            hint = "1 worst · 7 best",
            modifier = Modifier.padding(AppSpacing.lg),
        ) {
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
