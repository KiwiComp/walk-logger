package com.example.walklogger.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val SpinnerSize = 20.dp
private val SpinnerStroke = 2.dp

enum class AppButtonWidth {
    /** Fills the available width. */
    Fill,

    /** Shrinks to the label plus padding. */
    Wrap,
}

enum class AppButtonStyle {
    /** Solid action colour; the one action the screen is for ("Save walk"). */
    Primary,

    /** Light fill; an alternative to the primary action ("This walk already happened", "End walk"). */
    Secondary,

    /** Border only, for navy surfaces; the least emphasised action ("Pause"). */
    Tertiary,
}

/**
 * The app's action button.
 *
 * While [loading] the label is replaced by a spinner and clicks are ignored; the label
 * stays laid out (invisible) so the button keeps its width.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    width: AppButtonWidth = AppButtonWidth.Fill,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = { if (!loading) onClick() },
        enabled = enabled,
        shape = MaterialTheme.shapes.extraSmall,
        colors = when (style) {
            AppButtonStyle.Primary -> ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.action,
                contentColor = AppTheme.colors.onHeader,
                disabledContainerColor = AppTheme.colors.chip,
                disabledContentColor = AppTheme.colors.contentMuted,
            )

            AppButtonStyle.Secondary -> ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.card,
                contentColor = AppTheme.colors.content,
                disabledContainerColor = AppTheme.colors.chip,
                disabledContentColor = AppTheme.colors.contentMuted,
            )

            AppButtonStyle.Tertiary -> ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = AppTheme.colors.onHeader,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = AppTheme.colors.onHeaderMuted,
            )
        },
        border = when (style) {
            AppButtonStyle.Primary -> null

            AppButtonStyle.Secondary -> BorderStroke(1.dp, AppTheme.colors.outline)

            AppButtonStyle.Tertiary -> BorderStroke(
                1.dp,
                if (enabled) AppTheme.colors.outlineOnHeader else AppTheme.colors.onHeaderMuted,
            )
        },
        contentPadding = PaddingValues(horizontal = AppSpacing.xxl, vertical = AppSpacing.md),
        modifier = modifier
            .then(if (width == AppButtonWidth.Fill) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = AppSize.buttonHeight),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(if (loading) 0f else 1f),
            )
            if (loading) {
                CircularProgressIndicator(
                    color = LocalContentColor.current,
                    strokeWidth = SpinnerStroke,
                    modifier = Modifier.size(SpinnerSize),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppButtonPreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            AppButton(text = "Save walk", onClick = {})
            AppButton(text = "Save walk", onClick = {
            }, enabled = false, modifier = Modifier.padding(top = AppSpacing.lg))
            AppButton(text = "Save walk", onClick = {
            }, loading = true, modifier = Modifier.padding(top = AppSpacing.lg))
            AppButton(
                text = "Save walk",
                onClick = {},
                width = AppButtonWidth.Wrap,
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
            AppButton(
                text = "This walk already happened",
                onClick = {},
                style = AppButtonStyle.Secondary,
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
            AppButton(
                text = "This walk already happened",
                onClick = {},
                style = AppButtonStyle.Secondary,
                enabled = false,
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
        }
    }
}

@Preview
@Composable
private fun AppButtonTertiaryPreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.background(AppTheme.colors.header).padding(AppSpacing.lg)) {
            AppButton(text = "Pause", onClick = {}, style = AppButtonStyle.Tertiary)
            AppButton(
                text = "Pause",
                onClick = {},
                style = AppButtonStyle.Tertiary,
                enabled = false,
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
        }
    }
}
