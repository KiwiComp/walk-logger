package com.example.walklogger.feature.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.EmptyTabContent
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(title = "Calendar", actions = { DogBadge(name = "Sixten") })
        EmptyTabContent(message = "Calendar coming soon")
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    WalkLoggerTheme { CalendarScreen() }
}
