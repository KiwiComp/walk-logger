package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import kotlinx.coroutines.launch

enum class AppTextFieldStyle {
    /** Dark content on a light surface; every form field. */
    Default,

    /** Light text on the header surface; the quick note during a walk. */
    Inverse,
}

/**
 * Rounded, outlined text field with an optional bold [label] above it and a muted [labelHint]
 * ("Optional") at the end of the label row.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: AppTextFieldStyle = AppTextFieldStyle.Default,
    label: String? = null,
    labelHint: String? = null,
    placeholder: String? = null,
    suffix: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions =
        KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = when (style) {
        AppTextFieldStyle.Default -> FieldColors(
            text = AppTheme.colors.content,
            label = AppTheme.colors.contentSecondary,
            muted = AppTheme.colors.placeholder,
            container = AppTheme.colors.card,
            outline = AppTheme.colors.outlineStrong,
            focusedOutline = AppTheme.colors.action,
        )

        AppTextFieldStyle.Inverse -> FieldColors(
            text = AppTheme.colors.onHeader,
            label = AppTheme.colors.onHeaderMuted,
            muted = AppTheme.colors.onHeaderMuted,
            container = AppTheme.colors.headerRaised,
            outline = AppTheme.colors.outlineStrong,
            focusedOutline = AppTheme.colors.onHeader,
        )
    }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.sm)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.label,
                    modifier = Modifier.weight(1f),
                )
                labelHint?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted,
                    )
                }
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it) } },
            suffix = suffix?.let { { Text(it) } },
            trailingIcon = trailingIcon,
            supportingText = supportingText?.let { { Text(it) } },
            isError = isError,
            readOnly = readOnly,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(AppRadius.md),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text,
                focusedContainerColor = colors.container,
                unfocusedContainerColor = colors.container,
                errorContainerColor = colors.container,
                focusedBorderColor = colors.focusedOutline,
                unfocusedBorderColor = colors.outline,
                focusedPlaceholderColor = colors.muted,
                unfocusedPlaceholderColor = colors.muted,
                focusedSuffixColor = colors.muted,
                unfocusedSuffixColor = colors.muted,
                focusedTrailingIconColor = colors.label,
                unfocusedTrailingIconColor = colors.label,
                cursorColor = colors.focusedOutline,
            ),
            // Growing by a wrapped line keeps the new line above the keyboard.
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { isFocused = it.isFocused }
                .onSizeChanged {
                    if (isFocused) scope.launch { bringIntoViewRequester.bringIntoView() }
                },
        )
    }
}

private class FieldColors(
    val text: Color,
    val label: Color,
    val muted: Color,
    val container: Color,
    val outline: Color,
    val focusedOutline: Color,
)

@Preview(showBackground = true)
@Composable
private fun AppTextFieldPreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            AppTextField(value = "Sat 12 Sep", onValueChange = {}, label = "Date")
            AppTextField(
                value = "",
                onValueChange = {},
                label = "Weight",
                placeholder = "18",
                suffix = "kg",
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
            AppTextField(
                value = "heavy",
                onValueChange = {},
                label = "Weight",
                suffix = "kg",
                isError = true,
                supportingText = "Enter a number",
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
        }
    }
}

@Preview
@Composable
private fun AppTextFieldInversePreview() {
    WalkLoggerTheme {
        Column(modifier = Modifier.background(AppTheme.colors.header).padding(AppSpacing.lg)) {
            AppTextField(
                value = "",
                onValueChange = {},
                style = AppTextFieldStyle.Inverse,
                label = "Quick note while you walk",
                placeholder = "Barked at the bin lorry…",
            )
        }
    }
}
