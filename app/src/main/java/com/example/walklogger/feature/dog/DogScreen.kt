package com.example.walklogger.feature.dog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.EmptyTabContent
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** The dog's own tab. Title is the dog's name, so no badge in the header here. */
@Composable
fun DogScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(title = "Sixten")
        EmptyTabContent(message = stringResource(R.string.dog_profile_coming_soon))
    }
}

@Preview(showBackground = true)
@Composable
private fun DogScreenPreview() {
    WalkLoggerTheme { DogScreen() }
}
