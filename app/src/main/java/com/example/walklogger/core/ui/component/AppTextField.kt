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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.ChipSurface
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.SquareShape
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Square, filled text field with its label above, as in the "Log a past walk" form. */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    suffix: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    interactionSource: MutableInteractionSource? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            modifier = Modifier.padding(bottom = AppSpacing.xs),
        )
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
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                focusedContainerColor = ChipSurface,
                unfocusedContainerColor = ChipSurface,
                errorContainerColor = ChipSurface,
                focusedBorderColor = Steel,
                unfocusedBorderColor = Outline,
                focusedPlaceholderColor = InkMuted,
                unfocusedPlaceholderColor = InkMuted,
                focusedSuffixColor = InkMuted,
                unfocusedSuffixColor = InkMuted,
                cursorColor = Steel,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

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
