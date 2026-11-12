package com.example.walklogger.feature.patterns

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.EmptyTabContent
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

@Composable
fun PatternsRoute(modifier: Modifier = Modifier, viewModel: PatternsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PatternsScreen(uiState = uiState, modifier = modifier)
}

@Composable
fun PatternsScreen(uiState: PatternsUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.patterns_title),
            actions = {
                DogBadge(name = uiState.activeDogName, isDeleted = uiState.isActiveDogDeleted)
            },
        )
        EmptyTabContent(message = stringResource(R.string.patterns_coming_soon))
    }
}

@Preview(showBackground = true)
@Composable
private fun PatternsScreenPreview() {
    WalkLoggerTheme { PatternsScreen(uiState = PatternsUiState(activeDogName = "Sixten")) }
}
