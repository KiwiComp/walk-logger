package com.example.walklogger.core.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Read-only [AppTextField] that reports presses instead of edits, for values chosen in a picker. */
@Composable
fun AppPickerField(
    value: String,
    label: String,
    onPress: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnPress by rememberUpdatedState(onPress)
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) currentOnPress()
        }
    }
    AppTextField(
        value = value,
        onValueChange = {},
        label = label,
        placeholder = placeholder,
        readOnly = true,
        interactionSource = interactionSource,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun AppPickerFieldPreview() {
    WalkLoggerTheme {
        AppPickerField(value = "17:40", label = "Start", onPress = {
        }, modifier = Modifier.padding(AppSpacing.lg))
    }
}
