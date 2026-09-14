package com.example.walklogger.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.ChipSurface
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.OnNavy
import com.example.walklogger.core.ui.theme.SquareShape
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

enum class AppButtonWidth {
    /** Fills the available width. */
    Fill,

    /** Shrinks to the label plus padding. */
    Wrap,
}

/** The solid primary action button ("Save walk"). */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: AppButtonWidth = AppButtonWidth.Fill,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.extraSmall,
        colors = ButtonDefaults.buttonColors(
            containerColor = Steel,
            contentColor = OnNavy,
            disabledContainerColor = ChipSurface,
            disabledContentColor = InkMuted,
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.xxl, vertical = AppSpacing.md),
        modifier = modifier
            .then(if (width == AppButtonWidth.Fill) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = AppSize.buttonHeight),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

@Preview(showBackground = true)
@Composable
private fun AppButtonPreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            AppButton(text = "Save walk", onClick = {})
            AppButton(text = "Save walk", onClick = {}, enabled = false, modifier = Modifier.padding(top = AppSpacing.lg))
            AppButton(
                text = "Save walk",
                onClick = {},
                width = AppButtonWidth.Wrap,
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
        }
    }
}
