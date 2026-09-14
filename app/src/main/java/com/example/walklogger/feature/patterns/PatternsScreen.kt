package com.example.walklogger.feature.patterns

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.EmptyTabContent
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

@Composable
fun PatternsScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.patterns_title),
            actions = { DogBadge(name = "Sixten") },
        )
        EmptyTabContent(message = stringResource(R.string.patterns_coming_soon))
    }
}

@Preview(showBackground = true)
@Composable
private fun PatternsScreenPreview() {
    WalkLoggerTheme { PatternsScreen() }
}
