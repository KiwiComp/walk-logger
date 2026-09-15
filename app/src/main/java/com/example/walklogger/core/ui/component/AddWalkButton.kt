package com.example.walklogger.core.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Dashed call-to-action shown under a day's walks on the Log and Calendar tabs. */
@Composable
fun AddWalkButton(dayLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DashedButton(
        text = stringResource(R.string.add_walk_for_day, dayLabel),
        onClick = onClick,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun AddWalkButtonPreview() {
    WalkLoggerTheme {
        AddWalkButton(dayLabel = "Sat", onClick = {}, modifier = Modifier.padding(AppSpacing.lg))
    }
}
