package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val IconSize = 20.dp

enum class PillButtonStyle {
    /** On the chip colour; the usual look ("Edit"). */
    Filled,

    /** Bordered, for an action that sits beside a filled one ("Change"). */
    Outlined,

    /** Dark, for the main action in a header ("Edit" on walk detail). */
    Inverse,
}

/** Small rounded action at the end of a row, with an optional leading [icon]. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: PillButtonStyle = PillButtonStyle.Filled,
    enabled: Boolean = true,
) {
    val content = if (style ==
        PillButtonStyle.Inverse
    ) {
        AppTheme.colors.onSelection
    } else {
        AppTheme.colors.content
    }
    Row(
        modifier = modifier
            .clip(CircleShape)
            .then(
                when (style) {
                    PillButtonStyle.Filled -> Modifier.background(AppTheme.colors.chip)

                    PillButtonStyle.Outlined ->
                        Modifier.border(1.dp, AppTheme.colors.outlineStrong, CircleShape)

                    PillButtonStyle.Inverse -> Modifier.background(AppTheme.colors.selection)
                },
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(IconSize),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = content,
        )
    }
}

/** A [PillButton] that reads Edit with a pencil, or Done with a tick while [isEditing]. */
@Composable
fun EditDonePillButton(
    isEditing: Boolean,
    editLabel: String,
    doneLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    PillButton(
        text = if (isEditing) doneLabel else editLabel,
        icon = if (isEditing) Icons.Filled.Check else Icons.Outlined.Edit,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun PillButtonPreview() {
    WalkLoggerTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            PillButton(text = "Change", onClick = {}, style = PillButtonStyle.Outlined)
            PillButton(text = "Edit", onClick = {}, style = PillButtonStyle.Inverse)
            listOf(false, true).forEach {
                EditDonePillButton(
                    isEditing = it,
                    editLabel = "Edit",
                    doneLabel = "Done",
                    onClick = {},
                )
            }
        }
    }
}
