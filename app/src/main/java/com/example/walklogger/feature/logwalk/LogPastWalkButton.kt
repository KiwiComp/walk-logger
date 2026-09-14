package com.example.walklogger.feature.logwalk

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppButtonStyle
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Secondary action leading to the "Log a past walk" form. */
@Composable
internal fun LogPastWalkButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppButton(
        text = stringResource(R.string.new_walk_already_happened),
        onClick = onClick,
        style = AppButtonStyle.Secondary,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun LogPastWalkButtonPreview() {
    WalkLoggerTheme {
        LogPastWalkButton(onClick = {}, modifier = Modifier.padding(AppSpacing.lg))
    }
}
