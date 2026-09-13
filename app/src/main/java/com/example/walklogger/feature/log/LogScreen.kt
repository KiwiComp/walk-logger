package com.example.walklogger.feature.log

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/**
 * The Log tab. UI only for now: content is static sample data that mirrors the mockup.
 * A ViewModel will replace [sampleDay] and [sampleWeek] once the data layer is wired up.
 */
@Composable
fun LogScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = "Walk log",
            actions = { DogBadge(name = "Sixten") },
        ) {
            Spacer(Modifier.height(AppSpacing.xl))
            WeekStrip(days = sampleWeek)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
        ) {
            DayHeading(title = "Saturday 12 Sep", summary = "2 walks · avg 5.0")
            Spacer(Modifier.height(AppSpacing.lg))
            WalkTimeline(walks = sampleDay)
            AddWalkPlaceholder(dayLabel = "Sat", modifier = Modifier.padding(start = TimelineGutter))
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LogScreenPreview() {
    WalkLoggerTheme {
        LogScreen()
    }
}
