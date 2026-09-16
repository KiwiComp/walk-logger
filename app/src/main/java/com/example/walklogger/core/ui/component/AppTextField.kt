package com.example.walklogger.core.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.ChipSurface
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.Navy
import com.example.walklogger.core.ui.theme.NavyLight
import com.example.walklogger.core.ui.theme.OnNavy
import com.example.walklogger.core.ui.theme.OnNavyMuted
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.OutlineOnNavy
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

enum class AppTextFieldStyle {
    /** Ink on a light surface; every form field. */
    Default,

    /** Light text on the header surface; the quick note during a walk. */
    Inverse,
}

/** Square, filled text field with an optional [label] above, as in the "Log a past walk" form. */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: AppTextFieldStyle = AppTextFieldStyle.Default,
    label: String? = null,
    placeholder: String? = null,
    suffix: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = when (style) {
        AppTextFieldStyle.Default -> FieldColors(
            text = Ink,
            muted = InkMuted,
            container = ChipSurface,
            outline = Outline,
            focusedOutline = Steel,
        )
        AppTextFieldStyle.Inverse -> FieldColors(
            text = OnNavy,
            muted = OnNavyMuted,
            container = NavyLight,
            outline = OutlineOnNavy,
            focusedOutline = OnNavy,
        )
    }
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
                modifier = Modifier.padding(bottom = AppSpacing.xs),
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it) } },
            suffix = suffix?.let { { Text(it) } },
            supportingText = supportingText?.let { { Text(it) } },
            isError = isError,
            readOnly = readOnly,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            interactionSource = interactionSource,
            shape = MaterialTheme.shapes.extraSmall,
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
                cursorColor = colors.focusedOutline,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private class FieldColors(
    val text: Color,
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
        Column(modifier = Modifier.background(Navy).padding(AppSpacing.lg)) {
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
